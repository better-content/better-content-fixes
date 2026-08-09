package io.github.bcfixes.prestige;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrestigePerksCatalogTest {
    @Test
    void catalogHasThreeFourNodeBranchesAndUniqueIds() {
        List<PrestigePerks.Node> nodes = PrestigePerks.nodes();
        assertEquals(12, nodes.size());
        assertEquals(Set.of("Place", "Vessel", "Embark"), nodes.stream().map(PrestigePerks.Node::branch).collect(java.util.stream.Collectors.toSet()));
        assertEquals(12, nodes.stream().map(PrestigePerks.Node::id).collect(java.util.stream.Collectors.toSet()).size());
        assertTrue(nodes.stream().allMatch(node -> node.id().matches("[a-z0-9_]+")));
    }

    @Test
    void prerequisitesOnlyReferenceKnownNodesAndHaveNoCycles() {
        List<PrestigePerks.Node> nodes = PrestigePerks.nodes();
        Set<String> ids = nodes.stream().map(PrestigePerks.Node::id).collect(java.util.stream.Collectors.toSet());
        assertTrue(nodes.stream().flatMap(node -> node.prerequisites().stream()).allMatch(ids::contains));
        for (PrestigePerks.Node node : nodes) {
            assertFalse(node.prerequisites().contains(node.id()));
        }
        assertEquals(4, nodes.stream().filter(node -> node.branch().equals("Place")).count());
        assertEquals(4, nodes.stream().filter(node -> node.branch().equals("Vessel")).count());
        assertEquals(4, nodes.stream().filter(node -> node.branch().equals("Embark")).count());
    }
}
