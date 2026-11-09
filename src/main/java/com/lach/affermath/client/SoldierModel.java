package com.lach.affermath.client;

import com.lach.affermath.entity.HollowSoldier;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Hollow Soldier: helmet and brim, gas mask with swinging filter canister, pack, bedroll, greatcoat skirts. */
public class SoldierModel extends ZombieModel<HollowSoldier> {
    private final ModelPart coatFront, coatBack, canister;

    public SoldierModel(ModelPart root) {
        super(root);
        coatFront = body.getChild("coat_front");
        coatBack = body.getChild("coat_back");
        canister = head.getChild("snout").getChild("canister");
    }

    @Override
    public void setupAnim(HollowSoldier e, float limb, float amount, float age, float yaw, float pitch) {
        super.setupAnim(e, limb, amount, age, yaw, pitch);
        // dead twitch: a slow loll and the odd sharp jerk of the head
        head.zRot = Mth.sin(age * 0.07f) * 0.08f + (Mth.sin(age * 0.9f + e.getId()) > 0.985f ? 0.3f : 0f);
        head.xRot += 0.15f;
        hat.copyFrom(head);
        float stride = Mth.cos(limb * 0.6662f) * amount;
        coatFront.xRot = -Math.abs(stride) * 0.6f - amount * 0.2f;
        coatBack.xRot = Math.abs(stride) * 0.6f + amount * 0.25f + Mth.sin(age * 0.05f) * 0.04f;
        canister.xRot = 0.35f + Mth.sin(limb * 0.6662f * 2f) * 0.25f * amount + Mth.sin(age * 0.08f) * 0.05f;
    }
}
