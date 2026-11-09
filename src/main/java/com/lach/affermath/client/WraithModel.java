package com.lach.affermath.client;

import com.lach.affermath.entity.AshWraith;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class WraithModel extends HumanoidModel<AshWraith> {
    public float alpha = 1f;

    public WraithModel(ModelPart root) {
        super(root, RenderType::entityTranslucent);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0f);
        PartDefinition root = mesh.getRoot();
        // tattered robe tail instead of legs
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2f, 0f, -2f, 4, 12, 4, new CubeDeformation(-0.4f)),
                PartPose.offset(-1.9f, 12f, 0f));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 32).addBox(-2f, 0f, -2f, 4, 10, 4, new CubeDeformation(-0.6f)),
                PartPose.offset(1.9f, 12f, 0f));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(AshWraith e, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
        super.setupAnim(e, limbSwing, limbSwingAmount, age, yaw, pitch);
        AnimationUtils.animateZombieArms(leftArm, rightArm, e.isAggressive(), attackTime, age);
        rightLeg.xRot = 0.6f + Mth.sin(age * 0.1f) * 0.1f;
        leftLeg.xRot = 0.75f + Mth.cos(age * 0.1f) * 0.1f;
        rightLeg.yRot = leftLeg.yRot = 0;
        body.xRot = 0.15f;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer vc, int light, int overlay, int color) {
        int a = (int) (FastColor.ARGB32.alpha(color) * alpha);
        super.renderToBuffer(pose, vc, light, overlay, FastColor.ARGB32.color(a, color));
    }
}
