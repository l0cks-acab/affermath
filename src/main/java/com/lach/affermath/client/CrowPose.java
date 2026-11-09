package com.lach.affermath.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Shared crow animation so both Minecraft versions move the same. */
final class CrowPose {
    static void apply(ModelPart head, ModelPart lWing, ModelPart lTip, ModelPart rWing, ModelPart rTip, ModelPart tail,
                      ModelPart lLeg, ModelPart rLeg, boolean flying, float limb, float amount, float age, float yaw, float pitch, int id) {
        head.xRot = pitch * Mth.DEG_TO_RAD;
        head.yRot = yaw * Mth.DEG_TO_RAD;
        if (flying) {
            float flap = Mth.sin(age * 0.9f);
            lWing.zRot = -0.1f + flap * 0.75f;
            lTip.zRot = Mth.sin(age * 0.9f - 0.8f) * 0.55f;
            rWing.zRot = -lWing.zRot;
            rTip.zRot = -lTip.zRot;
            lLeg.xRot = rLeg.xRot = 1.1f;
            tail.xRot += 0.2f + Mth.sin(age * 0.45f) * 0.1f;
        } else {
            // folded: the span swings down along the flank and the tip doubles back over it
            lWing.zRot = 1.45f;
            lTip.zRot = -2.95f;
            rWing.zRot = -1.45f;
            rTip.zRot = 2.95f;
            lWing.yRot = -0.08f;
            rWing.yRot = 0.08f;
            lLeg.xRot = Mth.cos(limb * 1.3f) * 1.2f * amount;
            rLeg.xRot = Mth.cos(limb * 1.3f + Mth.PI) * 1.2f * amount;
            if (Mth.sin(age * 0.15f + id) > 0.95f) head.xRot += 0.8f; // peck at the rubble
            tail.xRot += Mth.sin(age * 0.2f) * 0.05f;
        }
    }

    private CrowPose() {}
}
