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
    void stagedAndResetContractsRoundTripExactly() throws Exception {
        Path stagedPath = temp.resolve("control/staged-request-v1.tsv");
        PrestigeContracts.Staged staged = new PrestigeContracts.Staged("lineage-abc", -42L, "any", "world");
        PrestigeContracts.writeStaged(stagedPath, staged);
        assertEquals(staged, PrestigeContracts.readStaged(stagedPath));

        Path resetPath = temp.resolve("control/reset-request-v1.tsv");
        PrestigeContracts.Reset reset = new PrestigeContracts.Reset(
                "lineage-abc", "transaction-abc", "world", 1L, -42L, "any");
        PrestigeContracts.writeReset(resetPath, reset);
        assertEquals(reset, PrestigeContracts.readReset(resetPath));
        assertTrue(Files.notExists(resetPath.resolveSibling("reset-request-v1.tsv.partial")));
    }

    @Test
    void contractsRejectUnknownFieldsOrderAndProfiles() throws Exception {
        Path malformed = temp.resolve("malformed.tsv");
        Files.writeString(malformed, PrestigeContracts.STAGED_MAGIC + "\n"
                + "successor_seed\t1\nlineage\tlineage-abc\nimpact\tany\nworld\tworld\n");
        assertThrows(IllegalArgumentException.class, () -> PrestigeContracts.readStaged(malformed));
        assertThrows(IllegalArgumentException.class,
                () -> PrestigeContracts.validateImpactProfile("minecraft:plains"));
        assertThrows(IllegalArgumentException.class,
                () -> PrestigeContracts.validateWorldName("../world"));
    }

    @Test
    void successorIdentityAndAttemptAreStrict() throws Exception {
        Path successor = temp.resolve("successor.tsv");
        Files.writeString(successor, PrestigeContracts.SUCCESSOR_MAGIC + "\n"
                + "lineage\tlineage-abc\ntransaction\ttransaction-abc\nsuccessor_seed\t7\nimpact\tany\nattempt\t4\n");
        assertThrows(IllegalArgumentException.class, () -> PrestigeContracts.readSuccessor(successor));
    }
}
