package com.lach.affermath.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

public class CrowModel extends EntityModel<CrowModel.State> {
    public static class State extends LivingEntityRenderState {
        public boolean flying;
        public int id;
    }

    private final ModelPart head, leftWing, rightWing, leftLeg, rightLeg;

    public CrowModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        r.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2f, 0f, -3f, 4, 4, 6), PartPose.offsetAndRotation(0, 17f, 0, -0.35f, 0, 0));
        r.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 10).addBox(-1.5f, -3f, -2f, 3, 3, 3)
                .texOffs(12, 10).addBox(-0.5f, -2f, -4f, 1, 1, 2), PartPose.offset(0, 17.5f, -2.5f));
        r.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(20, 0).addBox(0f, 0f, -2.5f, 1, 4, 5), PartPose.offset(2f, 17.5f, 0));
        r.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(20, 9).addBox(-1f, 0f, -2.5f, 1, 4, 5), PartPose.offset(-2f, 17.5f, 0));
        r.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 16).addBox(-1.5f, 0f, 0f, 3, 1, 4), PartPose.offsetAndRotation(0, 19.5f, 2.5f, 0.5f, 0, 0));
        r.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(14, 16).addBox(-0.5f, 0f, -0.5f, 1, 3, 1), PartPose.offset(1f, 21f, 0));
        r.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(14, 16).addBox(-0.5f, 0f, -0.5f, 1, 3, 1), PartPose.offset(-1f, 21f, 0));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(State s) {
        super.setupAnim(s);
        float age = s.ageInTicks;
        head.xRot = s.xRot * Mth.DEG_TO_RAD;
        head.yRot = s.yRot * Mth.DEG_TO_RAD;
        if (s.flying) {
            float f = Mth.sin(age * 1.4f) * 1.1f;
            leftWing.zRot = -0.3f - f;
            rightWing.zRot = 0.3f + f;
            leftLeg.xRot = rightLeg.xRot = 0.9f;
        } else {
            float limb = s.walkAnimationPos, amount = s.walkAnimationSpeed;
            leftLeg.xRot = Mth.cos(limb * 1.3f) * 1.2f * amount;
            rightLeg.xRot = Mth.cos(limb * 1.3f + Mth.PI) * 1.2f * amount;
            // peck at the rubble
            head.xRot += Math.max(0, Mth.sin(age * 0.15f + s.id)) > 0.95f ? 0.8f : 0;
        }
    }
}
