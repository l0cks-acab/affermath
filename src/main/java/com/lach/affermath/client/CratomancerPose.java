package com.lach.affermath.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Shared Cratomancer animation so both Minecraft versions move the same. */
final class CratomancerPose {
    static void apply(ModelPart body, ModelPart head, ModelPart stackL, ModelPart stackR, ModelPart mortar,
                      ModelPart rArm, ModelPart rFist, ModelPart lArm, ModelPart lFist, ModelPart rLeg, ModelPart lLeg,
                      float limb, float amount, float age, float yaw, float pitch, float attack, int phase) {
        float breathe = Mth.sin(age * 0.06f);
        float rage = phase >= 3 ? 1f : phase == 2 ? 0.5f : 0f;
        body.xRot += breathe * 0.03f + rage * 0.12f;
        body.y += breathe * 0.4f;
        head.yRot = yaw * Mth.DEG_TO_RAD * 0.8f;
        head.xRot = pitch * Mth.DEG_TO_RAD - body.xRot * 0.7f;

        // heavy, rolling gait
        float step = limb * 0.5f;
        rLeg.xRot = Mth.cos(step) * 0.9f * amount;
        lLeg.xRot = Mth.cos(step + Mth.PI) * 0.9f * amount;
        body.zRot = Mth.cos(step) * 0.06f * amount;
        body.yRot = Mth.sin(step) * 0.05f * amount;

        rArm.xRot = -0.15f + Mth.cos(step + Mth.PI) * 0.6f * amount + breathe * 0.04f;
        lArm.xRot = -0.15f + Mth.cos(step) * 0.6f * amount - breathe * 0.04f;
        rArm.zRot = 0.12f + rage * 0.1f;
        lArm.zRot = -0.12f - rage * 0.1f;
        rFist.xRot = -0.35f;
        lFist.xRot = -0.35f;

        // both fists come up over the head and hammer down
        if (attack > 0) {
            float a = Mth.sin(attack * Mth.PI);
            float down = attack > 0.5f ? (attack - 0.5f) * 2f : 0f;
            rArm.xRot = -2.7f * a + down * 0.6f;
            lArm.xRot = -2.7f * a + down * 0.6f;
            rArm.zRot = lArm.zRot = 0f;
            rFist.xRot = lFist.xRot = -0.6f * a;
            body.xRot += -0.25f * a + down * 0.35f;
        }

        stackL.xRot += Mth.sin(age * 0.6f) * 0.015f;
        stackR.xRot += Mth.sin(age * 0.6f + 1f) * 0.015f;
        mortar.xRot += Mth.sin(age * 0.04f) * 0.06f;
    }

    private CratomancerPose() {}
}
