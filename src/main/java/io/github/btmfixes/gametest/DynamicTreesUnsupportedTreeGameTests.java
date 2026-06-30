package io.github.btmfixes.gametest;

import io.github.btmfixes.BoundToMatterFixes;
import io.github.btmfixes.compat.DynamicTreesFallenTreeSweep;
import io.github.btmfixes.compat.DynamicTreesSupportSweep;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public final class DynamicTreesUnsupportedTreeGameTests {
    private DynamicTreesUnsupportedTreeGameTests() {
    }

    @GameTest(templateNamespace = BoundToMatterFixes.MOD_ID, template = "empty", timeoutTicks = 1200)
    public static void allRegisteredSpeciesLoseSupport(final GameTestHelper helper) {
        try {
            final var failures = DynamicTreesSupportSweep.run(helper.getLevel(), helper.absolutePos(new BlockPos(2, 2, 2)));
            if (!failures.isEmpty()) {
                helper.fail(String.join("; ", failures));
                return;
            }
            helper.succeed();
        } catch (ReflectiveOperationException e) {
            helper.fail("DT reflection failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    @GameTest(templateNamespace = BoundToMatterFixes.MOD_ID, template = "empty", timeoutTicks = 2400)
    public static void representativeTreesReconstructAsSettledLogsAndLeaves(final GameTestHelper helper) {
        try {
            final DynamicTreesFallenTreeSweep.SweepRunner runner =
                    DynamicTreesFallenTreeSweep.start(helper.getLevel(), helper.absolutePos(new BlockPos(20, 2, 20)));
            if (!runner.startNextRepresentative()) {
                final var failures = runner.failures();
                helper.fail(failures.isEmpty() ? "No Dynamic Trees representative species were available to test" : String.join("; ", failures));
                return;
            }
            continueRepresentativeSweep(helper, runner);
        } catch (ReflectiveOperationException e) {
            helper.fail("DT reflection failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static void continueRepresentativeSweep(final GameTestHelper helper,
                                                    final DynamicTreesFallenTreeSweep.SweepRunner runner) {
        try {
            switch (runner.poll()) {
                case PENDING -> helper.runAfterDelay(1, () -> continueRepresentativeSweep(helper, runner));
                case READY_FOR_NEXT -> {
                    if (runner.startNextRepresentative()) {
                        helper.runAfterDelay(1, () -> continueRepresentativeSweep(helper, runner));
                        return;
                    }
                    final var failures = runner.failures();
                    if (!failures.isEmpty()) {
                        helper.fail(String.join("; ", failures));
                        return;
                    }
                    helper.succeed();
                }
                case COMPLETE -> {
                    final var failures = runner.failures();
                    if (!failures.isEmpty()) {
                        helper.fail(String.join("; ", failures));
                        return;
                    }
                    helper.succeed();
                }
            }
        } catch (ReflectiveOperationException e) {
            helper.fail("DT reflection failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
