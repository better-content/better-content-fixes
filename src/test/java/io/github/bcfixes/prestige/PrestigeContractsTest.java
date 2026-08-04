package io.github.bcfixes.prestige;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrestigeContractsTest {
    @TempDir Path temp;

    @Test
    void v2ContractsRoundTripExactly() throws Exception {
        Path lineagePath = temp.resolve("lineage-v2.tsv");
        PrestigeContracts.Lineage lineage = new PrestigeContracts.Lineage("lineage-abc", 3, 3, 3);
        PrestigeContracts.writeLineage(lineagePath, lineage);
        assertEquals(lineage, PrestigeContracts.readLineage(lineagePath));

        Path stagedPath = temp.resolve("control/staged-request-v2.tsv");
        PrestigeContracts.Staged staged = new PrestigeContracts.Staged(
                "lineage-abc", "minecraft:plains", "Builder", "world");
        PrestigeContracts.writeStaged(stagedPath, staged);
        assertEquals(staged, PrestigeContracts.readStaged(stagedPath));

        Path resetPath = temp.resolve("control/reset-request-v2.tsv");
        PrestigeContracts.Reset reset = new PrestigeContracts.Reset(
                "lineage-abc", "transaction-abc", "world", 1L, "minecraft:plains");
        PrestigeContracts.writeReset(resetPath, reset);
        assertEquals(reset, PrestigeContracts.readReset(resetPath));
        assertTrue(Files.notExists(resetPath.resolveSibling("reset-request-v2.tsv.partial")));
    }

    @Test
    void contractsRejectUnknownOrderBiomeAndCounters() throws Exception {
        Path malformed = temp.resolve("malformed.tsv");
        Files.writeString(malformed, PrestigeContracts.STAGED_MAGIC + "\n"
                + "biome\tminecraft:plains\nlineage\tlineage-abc\nauthor\tBuilder\nworld\tworld\n");
        assertThrows(IllegalArgumentException.class, () -> PrestigeContracts.readStaged(malformed));
        assertThrows(IllegalArgumentException.class, () -> PrestigeContracts.validateBiome("../plains"));
        assertThrows(IllegalArgumentException.class, () -> PrestigeContracts.validateWorldName("../world"));
        assertThrows(IllegalArgumentException.class, () -> PrestigeContracts.writeLineage(
                temp.resolve("bad-lineage.tsv"), new PrestigeContracts.Lineage("lineage-abc", 1, 2, 1)));
    }

    @Test
    void successorIdentityAndAttemptAreStrict() throws Exception {
        Path successor = temp.resolve("successor.tsv");
        Files.writeString(successor, PrestigeContracts.SUCCESSOR_MAGIC + "\n"
                + "lineage\tlineage-abc\ntransaction\ttransaction-abc\nsuccessor_seed\t7\n"
                + "biome\tminecraft:plains\nattempt\t4\n");
        assertThrows(IllegalArgumentException.class, () -> PrestigeContracts.readSuccessor(successor));
    }
}
