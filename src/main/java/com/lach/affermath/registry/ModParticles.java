package com.lach.affermath.registry;

import com.lach.affermath.Affermath;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, Affermath.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ASH_FLAKE = PARTICLES.register("ash_flake", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ASH_STREAK = PARTICLES.register("ash_streak", () -> new SimpleParticleType(false));

    private ModParticles() {}
}
