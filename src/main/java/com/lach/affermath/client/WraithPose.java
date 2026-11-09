package com.lach.affermath.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Shared wraith animation so both Minecraft versions move the same. */
final class WraithPose {
    static void apply(float age, float amount, float yaw, float pitch, boolean aggressive, float attack,
                      ModelPart body, ModelPart head, ModelPart hoodTip, ModelPart rArm, ModelPart rFore, ModelPart rClaw,
                      ModelPart lArm, ModelPart lFore, ModelPart lClaw, ModelPart tail1, ModelPart tail2, ModelPart tail3) {
        float bob = Mth.sin(age * 0.08f);
        body.y += bob * 1.2f;
        body.xRot += amount * 0.35f;
        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot = pitch * Mth.DEG_TO_RAD - body.xRot * 0.5f;
        head.zRot = Mth.sin(age * 0.05f) * 0.08f;
        hoodTip.xRot += Mth.sin(age * 0.11f) * 0.12f - amount * 0.3f;

        float reach = aggressive ? -1.35f : -0.75f;
        float swipe = Mth.sin(attack * Mth.PI);
        rArm.xRot = reach + Mth.sin(age * 0.09f) * 0.12f - swipe * 0.9f;
        lArm.xRot = reach + Mth.sin(age * 0.09f + 1.3f) * 0.12f - swipe * 0.4f;
        rArm.yRot = swipe * 0.7f;
        rArm.zRot = 0.18f + Mth.sin(age * 0.07f) * 0.05f;
        lArm.zRot = -0.18f - Mth.sin(age * 0.07f) * 0.05f;
        rFore.xRot = aggressive ? -0.25f : -0.95f;
        lFore.xRot = aggressive ? -0.25f : -0.95f;
        float curl = aggressive ? -0.6f + Mth.sin(age * 0.3f) * 0.25f : -0.25f;
        rClaw.xRot = curl;
        lClaw.xRot = curl;

        float w = age * 0.12f;
        tail1.xRot = 0.12f + amount * 0.5f + Mth.sin(w) * 0.1f;
        tail2.xRot = 0.15f + amount * 0.3f + Mth.sin(w - 0.8f) * 0.16f;
        tail3.xRot = 0.2f + amount * 0.3f + Mth.sin(w - 1.6f) * 0.24f;
        tail1.zRot = Mth.sin(age * 0.07f) * 0.06f;
        tail2.zRot = Mth.sin(age * 0.07f - 0.7f) * 0.1f;
        tail3.zRot = Mth.sin(age * 0.07f - 1.4f) * 0.16f;
    }

    private WraithPose() {}
}
