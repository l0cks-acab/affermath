package com.lach.affermath.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/** Falling ash. Flakes drift and sway like snow; streaks drop fast like debris. */
public class AshParticle extends SingleQuadParticle {
    private final boolean streak;
    private final float swayPhase;
    private final float swayAmp;

    protected AshParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites, boolean streak, RandomSource rnd) {
        super(level, x, y, z, sprites.get(rnd));
        this.streak = streak;
        float g = streak ? 0.15f + random.nextFloat() * 0.2f : 0.45f + random.nextFloat() * 0.4f;
        setColor(g, g, g * 0.98f);
        alpha = streak ? 0.75f : 0.85f;
        quadSize = streak ? 0.18f + random.nextFloat() * 0.15f : 0.05f + random.nextFloat() * 0.07f;
        lifetime = streak ? 40 + random.nextInt(30) : 140 + random.nextInt(80);
        gravity = 0;
        hasPhysics = true;
        swayPhase = random.nextFloat() * Mth.TWO_PI;
        swayAmp = 0.01f + random.nextFloat() * 0.015f;
        xd = vx;
        yd = vy != 0 ? vy : (streak ? -0.35 - random.nextFloat() * 0.2 : -0.04 - random.nextFloat() * 0.04);
        zd = vz;
        roll = oRoll = random.nextFloat() * Mth.TWO_PI;
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        oRoll = roll;
        if (age++ >= lifetime) {
            remove();
            return;
        }
        if (!streak) {
            float t = age * 0.08f + swayPhase;
            xd = xd * 0.9 + Math.sin(t) * swayAmp;
            zd = zd * 0.9 + Math.cos(t * 0.7f) * swayAmp;
            roll += 0.03f;
        }
        move(xd, yd, zd);
        if (onGround) {
            xd = zd = 0;
            if (age < lifetime - 10) age = lifetime - 10;
            alpha *= 0.85f;
        }
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    public record Flake(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType t, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource r) {
            return new AshParticle(level, x, y, z, vx, vy, vz, sprites, false, r);
        }
    }

    public record Streak(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType t, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource r) {
            return new AshParticle(level, x, y, z, vx, vy, vz, sprites, true, r);
        }
    }
}
