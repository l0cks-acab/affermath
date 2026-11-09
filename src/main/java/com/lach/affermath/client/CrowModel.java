package com.lach.affermath.client;

import com.lach.affermath.entity.CinderCrow;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class CrowModel extends HierarchicalModel<CinderCrow> {
    private final ModelPart root, body, head, leftWing, rightWing, tail, leftLeg, rightLeg;

    public CrowModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
        this.tail = root.getChild("tail");
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
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(CinderCrow e, float limbSwing, float amount, float age, float yaw, float pitch) {
        head.xRot = pitch * Mth.DEG_TO_RAD;
        head.yRot = yaw * Mth.DEG_TO_RAD;
        boolean flying = e.isFlying();
        if (flying) {
            float f = Mth.sin(age * 1.4f) * 1.1f;
            leftWing.zRot = -0.3f - f;
            rightWing.zRot = 0.3f + f;
            leftLeg.xRot = rightLeg.xRot = 0.9f;
        } else {
            leftWing.zRot = rightWing.zRot = 0;
            leftLeg.xRot = Mth.cos(limbSwing * 1.3f) * 1.2f * amount;
            rightLeg.xRot = Mth.cos(limbSwing * 1.3f + Mth.PI) * 1.2f * amount;
            // peck at the rubble
            head.xRot += Math.max(0, Mth.sin(age * 0.15f + e.getId())) > 0.95f ? 0.8f : 0;
        }
    }
}
