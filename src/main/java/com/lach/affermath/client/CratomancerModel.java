package com.lach.affermath.client;

import com.lach.affermath.entity.Cratomancer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;

/** The Cratomancer: hunched iron hulk with exhaust stacks, a shoulder mortar, a glowing furnace core and huge gauntlets. */
public class CratomancerModel extends HierarchicalModel<Cratomancer> {
    private final ModelPart root;
    final ModelPart body, head, stackL, stackR, mortar, rArm, rFist, lArm, lFist, rLeg, lLeg;

    public CratomancerModel(ModelPart root) {
        this.root = root;
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
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(Cratomancer e, float limb, float amount, float age, float yaw, float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        CratomancerPose.apply(body, head, stackL, stackR, mortar, rArm, rFist, lArm, lFist, rLeg, lLeg,
                limb, amount, age, yaw, pitch, attackTime, e.getPhase());
    }
}
