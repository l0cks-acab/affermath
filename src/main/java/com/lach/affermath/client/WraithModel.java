package com.lach.affermath.client;

import com.lach.affermath.entity.AshWraith;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/** Ash Wraith: hooded skull, ragged mantle, long bone arms with claws, a three-piece robe tail that trails. */
public class WraithModel extends HierarchicalModel<AshWraith> {
    public float alpha = 1f;
    private final ModelPart root, body, head, hoodTip, rArm, rFore, rClaw, lArm, lFore, lClaw, tail1, tail2, tail3;

    public WraithModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.root = root;
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
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(AshWraith e, float limb, float amount, float age, float yaw, float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        WraithPose.apply(age, amount, yaw, pitch, e.isAggressive(), attackTime,
                body, head, hoodTip, rArm, rFore, rClaw, lArm, lFore, lClaw, tail1, tail2, tail3);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer vc, int light, int overlay, int color) {
        int a = (int) (FastColor.ARGB32.alpha(color) * alpha);
        super.renderToBuffer(pose, vc, light, overlay, FastColor.ARGB32.color(a, color));
    }
}
