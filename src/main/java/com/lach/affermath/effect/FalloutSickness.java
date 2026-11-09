package com.lach.affermath.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** Contaminated sludge poisoning: slow magic damage and a gnawing hunger. Stronger levels hit faster. */
public class FalloutSickness extends MobEffect {
    public FalloutSickness() {
        super(MobEffectCategory.HARMFUL, 0x8FA33A);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int every = Math.max(10, 50 >> amplifier);
        return duration % every == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity e, int amplifier) {
        e.hurt(e.damageSources().magic(), 1f);
        if (e instanceof Player p) p.causeFoodExhaustion(1.5f * (amplifier + 1));
        return true;
    }
}
