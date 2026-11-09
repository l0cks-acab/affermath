package com.lach.affermath.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** The Cratomancer: hunched iron hulk with exhaust stacks, a shoulder mortar, a glowing furnace core and huge gauntlets. */
public class CratomancerModel extends EntityModel<CratomancerModel.State> {
    public static class State extends LivingEntityRenderState {
        public float attackTime;
        public int phase;
    }

    private final ModelPart body, head, stackL, stackR, mortar, rArm, rFist, lArm, lFist, rLeg, lLeg;

    public CratomancerModel(ModelPart root) {
        super(root);
        body = root.getChild("body");
        head = body.getChild("head");
        stackL = body.getChild("stack_left");
        stackR = body.getChild("stack_right");
        mortar = body.getChild("mortar");
        rArm = root.getChild("right_arm");
        rFist = rArm.getChild("right_gauntlet");
        lArm = root.getChild("left_arm");
        lFist = lArm.getChild("left_gauntlet");
        rLeg = root.getChild("right_leg");
        lLeg = root.getChild("left_leg");
    }

    @Override
    public void setupAnim(State s) {
        super.setupAnim(s);
        CratomancerPose.apply(body, head, stackL, stackR, mortar, rArm, rFist, lArm, lFist, rLeg, lLeg,
                s.walkAnimationPos, s.walkAnimationSpeed, s.ageInTicks, s.yRot, s.xRot, s.attackTime, s.phase);
    }
}
