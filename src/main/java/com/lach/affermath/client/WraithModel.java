package com.lach.affermath.client;

import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.UndeadRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

public class WraithModel extends HumanoidModel<WraithModel.State> {
    public static class State extends UndeadRenderState {
        public boolean aggressive;
        public float alpha = 1f;
    }

    public WraithModel(ModelPart root) {
        super(root, RenderTypes::entityTranslucent);
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
    public void setupAnim(State s) {
        super.setupAnim(s);
        float age = s.ageInTicks;
        AnimationUtils.animateZombieArms(leftArm, rightArm, s.aggressive, s);
        rightLeg.xRot = 0.6f + Mth.sin(age * 0.1f) * 0.1f;
        leftLeg.xRot = 0.75f + Mth.cos(age * 0.1f) * 0.1f;
        rightLeg.yRot = leftLeg.yRot = 0;
        body.xRot = 0.15f;
    }
}
