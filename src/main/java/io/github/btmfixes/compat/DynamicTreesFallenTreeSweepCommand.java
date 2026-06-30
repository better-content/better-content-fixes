package io.github.btmfixes.compat;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

public final class DynamicTreesFallenTreeSweepCommand {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static ActiveSweep activeSweep;

    private DynamicTreesFallenTreeSweepCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(final RegisterCommandsEvent event) {
        final CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("btmfixes_dt_fallen_tree_sweep")
                .requires(source -> source.hasPermission(2))
                .executes(ctx -> run(ctx.getSource())));
    }

    @SubscribeEvent
    public static void onServerTick(final TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || activeSweep == null) {
            return;
        }

        try {
            switch (activeSweep.runner.poll()) {
                case PENDING -> {
                    return;
                }
                case READY_FOR_NEXT -> {
                    if (activeSweep.runner.startNextRepresentative()) {
                        return;
                    }
                }
                case COMPLETE -> {
                }
            }

            final var failures = activeSweep.runner.failures();
            if (failures.isEmpty()) {
                LOGGER.info("[BTMFIXES] DT fallen-tree sweep passed: reconstructed logs and leaves remained settled.");
                activeSweep.source.sendSuccess(
                        () -> Component.literal("[BTMFIXES] DT fallen-tree sweep passed: reconstructed logs and leaves remained settled."),
                        true
                );
            } else {
                LOGGER.error("[BTMFIXES] DT fallen-tree sweep failures: {}", String.join("; ", failures));
                activeSweep.source.sendFailure(Component.literal("[BTMFIXES] DT fallen-tree sweep failures: " + String.join("; ", failures)));
            }
        } catch (ReflectiveOperationException e) {
            LOGGER.error("[BTMFIXES] DT fallen-tree sweep reflection failure", e);
            activeSweep.source.sendFailure(Component.literal("[BTMFIXES] DT fallen-tree sweep reflection failure: " + e.getMessage()));
        } catch (Throwable t) {
            LOGGER.error("[BTMFIXES] DT fallen-tree sweep unexpected failure", t);
            activeSweep.source.sendFailure(Component.literal("[BTMFIXES] DT fallen-tree sweep unexpected failure: "
                    + t.getClass().getSimpleName() + ": " + t.getMessage()));
        } finally {
            activeSweep = null;
        }
    }

    private static int run(final CommandSourceStack source) {
        if (activeSweep != null) {
            source.sendFailure(Component.literal("[BTMFIXES] DT fallen-tree sweep is already running."));
            return 0;
        }

        final ServerLevel level = source.getServer().overworld();
        try {
            final BlockPos origin = BlockPos.containing(source.getPosition()).offset(20, 4, 20);
            final DynamicTreesFallenTreeSweep.SweepRunner runner = DynamicTreesFallenTreeSweep.start(level, origin);
            if (!runner.startNextRepresentative()) {
                final var failures = runner.failures();
                source.sendFailure(Component.literal(failures.isEmpty()
                        ? "[BTMFIXES] DT fallen-tree sweep found no representative species to test."
                        : "[BTMFIXES] DT fallen-tree sweep setup failures: " + String.join("; ", failures)));
                return 0;
            }

            activeSweep = new ActiveSweep(source, runner);
            LOGGER.info("[BTMFIXES] Started DT fallen-tree sweep. Waiting for settled post-physics results.");
            source.sendSuccess(
                    () -> Component.literal("[BTMFIXES] Started DT fallen-tree sweep. Waiting for settled post-physics results."),
                    true
            );
            return 1;
        } catch (ReflectiveOperationException e) {
            LOGGER.error("[BTMFIXES] DT fallen-tree sweep reflection failure", e);
            source.sendFailure(Component.literal("[BTMFIXES] DT fallen-tree sweep reflection failure: " + e.getMessage()));
            return 0;
        }
    }

    private record ActiveSweep(CommandSourceStack source, DynamicTreesFallenTreeSweep.SweepRunner runner) {
    }
}
