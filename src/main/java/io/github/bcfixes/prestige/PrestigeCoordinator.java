package io.github.bcfixes.prestige;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PrestigeCoordinator {
    private static int stopCountdown = -1;
    private static int shutdownPoll = 0;

    private PrestigeCoordinator() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("prestige")
                .requires(source -> source.hasPermission(4))
                .then(Commands.literal("prototype")
                        .then(Commands.literal("stage")
                                .then(Commands.argument("seed", LongArgumentType.longArg())
                                        .then(Commands.argument("impact-profile", StringArgumentType.word())
                                                .executes(context -> stage(
                                                        context.getSource(),
                                                        LongArgumentType.getLong(context, "seed"),
                                                        StringArgumentType.getString(context, "impact-profile"))))))
                        .then(Commands.literal("status").executes(context -> status(context.getSource())))
                        .then(Commands.literal("cancel").executes(context -> cancel(context.getSource())))
                        .then(Commands.literal("commit").executes(context -> commit(context.getSource())))));
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        Path successorPath = control(server).resolve("successor-request-v1.tsv");
        if (!Files.isRegularFile(successorPath)) return;
        try {
            PrestigeContracts.Successor successor = PrestigeContracts.readSuccessor(successorPath);
            long actualSeed = server.overworld().getSeed();
            ResourceLocation biome = server.overworld().getBiome(server.overworld().getSharedSpawnPos())
                    .unwrapKey().orElseThrow(() -> new IllegalStateException("spawn biome has no registry key")).location();
            String worldName = worldName(server);
            if (!Files.isRegularFile(server.getWorldPath(LevelResource.LEVEL_DATA_FILE))) {
                throw new IllegalStateException("successor level.dat is not present at ServerStartedEvent");
            }
            PrestigeContracts.writeHealth(control(server).resolve("health-result-v1.tsv"), successor,
                    actualSeed, biome.toString(), worldName);
            server.sendSystemMessage(Component.literal("Prestige successor health published for " + successor.transactionId()));
        } catch (Exception error) {
            server.sendSystemMessage(Component.literal("Prestige successor health failed: " + error.getMessage()));
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        if (stopCountdown >= 0 && --stopCountdown <= 0) {
            stopCountdown = -1;
            server.halt(false);
            return;
        }
        if (++shutdownPoll < 20) return;
        shutdownPoll = 0;
        Path shutdownPath = control(server).resolve("shutdown-request-v1.tsv");
        if (!Files.isRegularFile(shutdownPath)) return;
        try {
            String requestedTransaction = PrestigeContracts.readShutdownTransaction(shutdownPath);
            Path successorPath = control(server).resolve("successor-request-v1.tsv");
            if (Files.isRegularFile(successorPath)
                    && PrestigeContracts.readSuccessor(successorPath).transactionId().equals(requestedTransaction)) {
                Files.deleteIfExists(shutdownPath);
                server.saveEverything(true, true, true);
                server.halt(false);
            }
        } catch (Exception error) {
            server.sendSystemMessage(Component.literal("Ignoring invalid prestige shutdown request: " + error.getMessage()));
        }
    }

    private static int stage(CommandSourceStack source, long seed, String impactProfile) {
        MinecraftServer server = source.getServer();
        try {
            PrestigeContracts.validateImpactProfile(impactProfile);
            Path control = control(server);
            if (Files.exists(control.resolve("reset-request-v1.tsv"))) {
                throw new IllegalStateException("a committed reset already exists");
            }
            PrestigeContracts.Lineage lineage = PrestigeContracts.readLineage(state(server).resolve("lineage-v1.tsv"));
            PrestigeContracts.writeStaged(control.resolve("staged-request-v1.tsv"),
                    new PrestigeContracts.Staged(lineage.lineageId(), seed, impactProfile, worldName(server)));
            source.sendSuccess(() -> Component.literal("Staged prestige successor seed=" + seed + " impact=" + impactProfile), true);
            return 1;
        } catch (Exception error) {
            source.sendFailure(Component.literal("Prestige stage refused: " + error.getMessage()));
            return 0;
        }
    }

    private static int status(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        Path control = control(server);
        String status = Files.exists(control.resolve("reset-request-v1.tsv")) ? "committed"
                : Files.exists(control.resolve("staged-request-v1.tsv")) ? "staged"
                : Files.exists(control.resolve("successor-request-v1.tsv")) ? "successor-starting" : "idle";
        source.sendSuccess(() -> Component.literal("Prestige prototype status: " + status), false);
        return 1;
    }

    private static int cancel(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        try {
            if (Files.exists(control(server).resolve("reset-request-v1.tsv"))) {
                throw new IllegalStateException("committed resets cannot be cancelled in-game");
            }
            boolean removed = Files.deleteIfExists(control(server).resolve("staged-request-v1.tsv"));
            source.sendSuccess(() -> Component.literal(removed ? "Cancelled staged prestige reset" : "No staged prestige reset"), true);
            return 1;
        } catch (Exception error) {
            source.sendFailure(Component.literal("Prestige cancel refused: " + error.getMessage()));
            return 0;
        }
    }

    private static int commit(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        try {
            Path control = control(server);
            if (Files.exists(control.resolve("reset-request-v1.tsv"))) {
                throw new IllegalStateException("a committed reset already exists");
            }
            PrestigeContracts.Staged staged = PrestigeContracts.readStaged(control.resolve("staged-request-v1.tsv"));
            PrestigeContracts.Lineage lineage = PrestigeContracts.readLineage(state(server).resolve("lineage-v1.tsv"));
            if (!staged.lineageId().equals(lineage.lineageId())) throw new IllegalStateException("staged lineage is stale");
            if (!staged.worldName().equals(worldName(server))) throw new IllegalStateException("staged world identity changed");
            String transaction = PrestigeContracts.newTransactionId();
            server.getPlayerList().saveAll();
            if (!server.saveEverything(true, true, true)) throw new IllegalStateException("Minecraft save finalization failed");
            PrestigeContracts.writeReset(control.resolve("reset-request-v1.tsv"), new PrestigeContracts.Reset(
                    lineage.lineageId(), transaction, staged.worldName(), server.overworld().getSeed(),
                    staged.successorSeed(), staged.impactProfile()));
            Files.delete(control.resolve("staged-request-v1.tsv"));
            source.sendSuccess(() -> Component.literal("Committed prestige reset " + transaction + "; server stopping"), true);
            stopCountdown = 20;
            return 1;
        } catch (Exception error) {
            source.sendFailure(Component.literal("Prestige commit refused: " + error.getMessage()));
            return 0;
        }
    }

    private static Path state(MinecraftServer server) {
        return server.getServerDirectory().toPath().toAbsolutePath().normalize().resolve(".prestige");
    }

    private static Path control(MinecraftServer server) {
        return state(server).resolve("control");
    }

    private static String worldName(MinecraftServer server) {
        Path root = server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
        String name = root.getFileName().toString();
        PrestigeContracts.validateWorldName(name);
        return name;
    }
}
