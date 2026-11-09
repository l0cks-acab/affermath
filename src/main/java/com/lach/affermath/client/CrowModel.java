package com.lach.affermath.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Cinder Crow: two-piece wings that fold against the body and spread wide in flight, fan tail, ember eyes. */
public class CrowModel extends EntityModel<CrowModel.State> {
    public static class State extends LivingEntityRenderState {
        public boolean flying;
        public int id;
    }

    private final ModelPart head, lWing, lTip, rWing, rTip, tail, lLeg, rLeg;

    public CrowModel(ModelPart root) {
        super(root);
        head = root.getChild("head");
        lWing = root.getChild("left_wing");
        lTip = lWing.getChild("left_wing_tip");
        rWing = root.getChild("right_wing");
        rTip = rWing.getChild("right_wing_tip");
        tail = root.getChild("tail");
        lLeg = root.getChild("left_leg");
        rLeg = root.getChild("right_leg");
    }

    @Override
    public void setupAnim(State s) {
        super.setupAnim(s);
        CrowPose.apply(head, lWing, lTip, rWing, rTip, tail, lLeg, rLeg, s.flying, s.walkAnimationPos, s.walkAnimationSpeed,
                s.ageInTicks, s.yRot, s.xRot, s.id);
    }
}
