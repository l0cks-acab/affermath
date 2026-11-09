package com.lach.affermath.registry;

import com.lach.affermath.Affermath;
import com.lach.affermath.effect.FalloutSickness;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Affermath.MODID);
    public static final DeferredHolder<MobEffect, FalloutSickness> FALLOUT_SICKNESS = EFFECTS.register("fallout_sickness", FalloutSickness::new);

    private ModEffects() {}
}
