package com.lach.affermath.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/** Ash Wraith: hooded skull, ragged mantle, long bone arms with claws, a three-piece robe tail that trails. */
public class WraithModel extends EntityModel<WraithModel.State> {
    public static class State extends LivingEntityRenderState {
        public boolean aggressive;
        public float attackTime;
        public float alpha = 1f;
    }

    private final ModelPart body, head, hoodTip, rArm, rFore, rClaw, lArm, lFore, lClaw, tail1, tail2, tail3;

    public WraithModel(ModelPart root) {
        super(root, RenderTypes::entityTranslucent);
        body = root.getChild("body");
        head = body.getChild("head");
        hoodTip = head.getChild("hood").getChild("hood_tip");
        rArm = body.getChild("right_arm");
        rFore = rArm.getChild("right_forearm");
        rClaw = rFore.getChild("right_claws");
        lArm = body.getChild("left_arm");
        lFore = lArm.getChild("left_forearm");
        lClaw = lFore.getChild("left_claws");
        tail1 = body.getChild("tail1");
        tail2 = tail1.getChild("tail2");
        tail3 = tail2.getChild("tail3");
    }

    @Override
    public void setupAnim(State s) {
        super.setupAnim(s);
        WraithPose.apply(s.ageInTicks, s.walkAnimationSpeed, s.yRot, s.xRot, s.aggressive, s.attackTime,
                body, head, hoodTip, rArm, rFore, rClaw, lArm, lFore, lClaw, tail1, tail2, tail3);
    }
}
