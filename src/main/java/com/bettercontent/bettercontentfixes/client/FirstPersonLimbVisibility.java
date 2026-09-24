package com.bettercontent.bettercontentfixes.client;

import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.client.mesh.HumanoidMesh;

public final class FirstPersonLimbVisibility {
    private static final List<String> ARM_PARTS = List.of("leftArm", "rightArm", "leftSleeve", "rightSleeve");

    private FirstPersonLimbVisibility() {
    }

    public static void hideLowerBodyLimbs(final HumanoidMesh mesh) {
        // Epic Fight supplies the active attack/swim pose through these bones. Hiding them here
        // overrides its per-animation visibility and removes the first-person attack motion.
        mesh.leftLeg.setHidden(true);
        mesh.rightLeg.setHidden(true);
        mesh.leftPants.setHidden(true);
        mesh.rightPants.setHidden(true);
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
