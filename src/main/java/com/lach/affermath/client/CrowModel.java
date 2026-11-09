package com.lach.affermath.client;

import com.lach.affermath.entity.CinderCrow;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;

/** Cinder Crow: two-piece wings that fold against the body and spread wide in flight, fan tail, ember eyes. */
public class CrowModel extends HierarchicalModel<CinderCrow> {
    private final ModelPart root;
    final ModelPart head, lWing, lTip, rWing, rTip, tail, lLeg, rLeg;

    public CrowModel(ModelPart root) {
        this.root = root;
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
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(CinderCrow e, float limb, float amount, float age, float yaw, float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        CrowPose.apply(this.head, lWing, lTip, rWing, rTip, tail, lLeg, rLeg, e.isFlying(), limb, amount, age, yaw, pitch, e.getId());
    }
}
