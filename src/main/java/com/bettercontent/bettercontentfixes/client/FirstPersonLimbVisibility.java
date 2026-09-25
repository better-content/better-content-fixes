package com.bettercontent.bettercontentfixes.client;

import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.client.mesh.HumanoidMesh;

public final class FirstPersonLimbVisibility {
    private static final List<String> ARM_PARTS = List.of("leftArm", "rightArm", "leftSleeve", "rightSleeve");

    private FirstPersonLimbVisibility() {
    }

    public static void hidePlayerModel(final HumanoidMesh mesh) {
        // Hide every player-model part, including the head and torso. Held items and armor are
        // rendered by separate layers and keep their own visibility policy.
        mesh.getAllParts().forEach(part -> part.setHidden(true));
    }

    public static void hideArmorLimbs(final SkinnedMesh mesh, final EquipmentSlot slot) {
        if (slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET) {
            mesh.getAllParts().forEach(part -> part.setHidden(true));
            return;
        }
        if (slot != EquipmentSlot.CHEST) {
            return;
        }
        ARM_PARTS.stream()
                .filter(mesh::hasPart)
                .map(mesh::getPart)
                .forEach(part -> part.setHidden(true));
    }
}
