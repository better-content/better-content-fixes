package io.github.bcfixes.prestige;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.nio.file.Files;
import java.nio.file.Path;

public final class PrestigeCoordinator {
    private static int stopCountdown = -1;
    private static int shutdownPoll = 0;

    private PrestigeCoordinator() {}

    public static void scheduleStop() { stopCountdown = 20; }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("prestige")
                .then(Commands.literal("status").executes(context -> {
                    try {
                        var lineage = PrestigeService.lineage(context.getSource().getServer());
                        context.getSource().sendSuccess(() -> Component.literal("Prestige generation=" + lineage.generation()
                                + " total=" + lineage.totalPrestiges() + " unspent=" + lineage.unspentPoints()), false);
                        return 1;
                    } catch (Exception error) {
                        context.getSource().sendFailure(Component.literal("Prestige status failed: " + error.getMessage()));
                        return 0;
                    }
                }))
                .then(Commands.literal("recovery").requires(source -> source.hasPermission(4))
                        .then(Commands.literal("cancel-staged").executes(context -> {
                            try {
                                Files.deleteIfExists(PrestigeService.control(context.getSource().getServer()).resolve("staged-request-v2.tsv"));
                                context.getSource().sendSuccess(() -> Component.literal("Cancelled staged prestige request"), true);
                                return 1;
                            } catch (Exception error) {
                                context.getSource().sendFailure(Component.literal("Recovery cancel failed: " + error.getMessage()));
                                return 0;
                            }
                        }))));
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        Path successorPath = PrestigeService.control(server).resolve("successor-request-v2.tsv");
        if (!Files.isRegularFile(successorPath)) return;
        try {
            PrestigeContracts.Successor successor = PrestigeContracts.readSuccessor(successorPath);
            ServerLevel level = server.overworld();
            ResourceLocation requested = new ResourceLocation(successor.biome());
            Pair<BlockPos, Holder<Biome>> found = level.findClosestBiome3d(holder -> holder.unwrapKey()
                    .map(key -> key.location().equals(requested)).orElse(false), level.getSharedSpawnPos(), 16_384, 32, 64);
            boolean foundExact = found != null;
            BlockPos spawn = level.getSharedSpawnPos();
            if (foundExact) {
                BlockPos candidate = found.getFirst();
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidate.getX(), candidate.getZ());
                spawn = new BlockPos(candidate.getX(), y, candidate.getZ());
                level.setDefaultSpawnPos(spawn, 0.0F);
            }
            String actualBiome = level.getBiome(spawn).unwrapKey()
                    .map(key -> key.location().toString()).orElse("minecraft:the_void");
            boolean fresh = freshDirectory(server.getWorldPath(LevelResource.PLAYER_DATA_DIR))
                    && freshDirectory(server.getWorldPath(LevelResource.PLAYER_ADVANCEMENTS_DIR))
                    && freshDirectory(server.getWorldPath(LevelResource.PLAYER_STATS_DIR))
                    && server.getPlayerCount() == 0 && foundExact;
            if (!Files.isRegularFile(server.getWorldPath(LevelResource.LEVEL_DATA_FILE))) {
                throw new IllegalStateException("successor level.dat is missing");
            }
            PrestigeContracts.writeHealth(PrestigeService.control(server).resolve("health-result-v2.tsv"), successor,
                    level.getSeed(), actualBiome, PrestigeService.worldName(server), fresh);
            server.sendSystemMessage(Component.literal("Prestige successor health published for " + successor.transactionId()
                    + " biome=" + actualBiome));
        } catch (Exception error) {
            server.sendSystemMessage(Component.literal("Prestige successor health failed: " + error.getMessage()));
        }
    }

    private static boolean freshDirectory(Path path) throws java.io.IOException {
        if (!Files.exists(path)) return true;
        if (!Files.isDirectory(path)) return false;
        try (var entries = Files.list(path)) { return entries.findAny().isEmpty(); }
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
        Path shutdownPath = PrestigeService.control(server).resolve("shutdown-request-v2.tsv");
        if (!Files.isRegularFile(shutdownPath)) return;
        try {
            String transaction = PrestigeContracts.readShutdownTransaction(shutdownPath);
            Path successorPath = PrestigeService.control(server).resolve("successor-request-v2.tsv");
            if (Files.isRegularFile(successorPath)
                    && PrestigeContracts.readSuccessor(successorPath).transactionId().equals(transaction)) {
                Files.deleteIfExists(shutdownPath);
                server.saveEverything(true, true, true);
                server.halt(false);
            }
        } catch (Exception error) {
            server.sendSystemMessage(Component.literal("Ignoring invalid prestige shutdown request: " + error.getMessage()));
        }
    }
}
