package com.lach.affermath.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.util.Mth;

/** Hollow Soldier: helmet and brim, gas mask with swinging filter canister, pack, bedroll, greatcoat skirts. */
public class SoldierModel extends ZombieModel<SoldierModel.State> {
    public static class State extends ZombieRenderState {
        public int id;
    }

    private final ModelPart coatFront, coatBack, canister;

    public SoldierModel(ModelPart root) {
        super(root);
        coatFront = body.getChild("coat_front");
        coatBack = body.getChild("coat_back");
        canister = head.getChild("snout").getChild("canister");
    }

    @Override
    public void setupAnim(State s) {
        super.setupAnim(s);
        float age = s.ageInTicks, limb = s.walkAnimationPos, amount = s.walkAnimationSpeed;
        head.zRot = Mth.sin(age * 0.07f) * 0.08f + (Mth.sin(age * 0.9f + s.id) > 0.985f ? 0.3f : 0f);
        head.xRot += 0.15f;
        float stride = Mth.cos(limb * 0.6662f) * amount;
        coatFront.xRot = -Math.abs(stride) * 0.6f - amount * 0.2f;
        coatBack.xRot = Math.abs(stride) * 0.6f + amount * 0.25f + Mth.sin(age * 0.05f) * 0.04f;
        canister.xRot = 0.35f + Mth.sin(limb * 0.6662f * 2f) * 0.25f * amount + Mth.sin(age * 0.08f) * 0.05f;
    }
}
