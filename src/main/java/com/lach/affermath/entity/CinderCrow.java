package com.lach.affermath.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Harmless ambient crow that picks through rubble. */
public class CinderCrow extends PathfinderMob implements FlyingAnimal {
    public float flap, flapSpeed, oFlapSpeed, oFlap;
    private float flapping = 1f;
    private float nextFlap = 1f;

    public CinderCrow(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        moveControl = new FlyingMoveControl(this, 10, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6)
                .add(Attributes.FLYING_SPEED, 0.4)
                .add(Attributes.MOVEMENT_SPEED, 0.2);
    }

    public static boolean checkSpawnRules(EntityType<CinderCrow> type, LevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).isSolid() && level.canSeeSky(pos);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 6f, 1.2, 1.5));
        goalSelector.addGoal(3, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8f));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        oFlap = flap;
        oFlapSpeed = flapSpeed;
        flapSpeed += (!onGround() && !isPassenger() ? 4 : -1) * 0.3f;
        flapSpeed = Mth.clamp(flapSpeed, 0f, 1f);
        if (!onGround() && flapping < 1f) flapping = 1f;
        flapping *= 0.9f;
        Vec3 v = getDeltaMovement();
        if (!onGround() && v.y < 0) setDeltaMovement(v.multiply(1, 0.6, 1));
        flap += flapping * 2f;
    }

    @Override
    protected boolean isFlapping() {
        return flyDist > nextFlap;
    }

    @Override
    protected void onFlap() {
        playSound(SoundEvents.PARROT_FLY, 0.1f, 0.8f);
        nextFlap = flyDist + flapSpeed / 2f;
    }

    @Override
    public boolean isFlying() {
        return !onGround();
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource src) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PARROT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource src) {
        return SoundEvents.PARROT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PARROT_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.45f + random.nextFloat() * 0.1f;
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return true;
    }
}
