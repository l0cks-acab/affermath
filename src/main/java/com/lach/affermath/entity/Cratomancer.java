package com.lach.affermath.entity;

import com.lach.affermath.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * The Cratomancer: gas-masked war-mage at the bottom of a pre-war bunker.
 * Phase 1 mortar strikes, phase 2 (50%) summons soldiers and thickens fog, phase 3 (25%) crater slams.
 */
public class Cratomancer extends Monster {
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(Cratomancer.class, EntityDataSerializers.INT);
    private static final DustParticleOptions MARK = new DustParticleOptions(new Vector3f(0.85f, 0.35f, 0.12f), 1.4f);

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(getDisplayName(),
            BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.NOTCHED_10).setDarkenScreen(true).setCreateWorldFog(true);
    private final List<Strike> strikes = new ArrayList<>();
    private int mortarCooldown = 60;
    private int summonCooldown = 0;
    private int slamCooldown = 100;
    private boolean slamming;
    private int slamAirTicks;

    private BlockPos home;

    /** Keeps the boss inside its vault. */
    public void setHome(BlockPos pos) {
        home = pos;
        restrictTo(pos, 9);
    }

    private record Strike(Vec3 pos, int[] fuse) {}

    public Cratomancer(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        xpReward = 150;
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 320)
                .add(Attributes.ARMOR, 12)
                .add(Attributes.ARMOR_TOUGHNESS, 4)
                .add(Attributes.ATTACK_DAMAGE, 11)
                .add(Attributes.ATTACK_KNOCKBACK, 1.2)
                .add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 40)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        super.defineSynchedData(b);
        b.define(PHASE, 1);
    }

    public int getPhase() {
        return entityData.get(PHASE);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16f));
        targetSelector.addGoal(1, new HurtByTargetGoal(this, HollowSoldier.class));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        ServerLevel level = (ServerLevel) level();
        float frac = getHealth() / getMaxHealth();
        int phase = frac <= 0.25f ? 3 : frac <= 0.5f ? 2 : 1;
        if (phase > getPhase()) enterPhase(level, phase);
        bossEvent.setProgress(frac);

        LivingEntity target = getTarget();
        if (target != null && target.isAlive()) {
            if (--mortarCooldown <= 0) {
                callMortars(level, target, phase);
                mortarCooldown = phase == 3 ? 70 : phase == 2 ? 95 : 120;
            }
            if (phase >= 2 && --summonCooldown <= 0) {
                summon(level, 2 + random.nextInt(2));
                summonCooldown = 420;
            }
            if (phase >= 3 && --slamCooldown <= 0 && onGround()) {
                slamming = true;
                slamAirTicks = 0;
                setDeltaMovement(getDeltaMovement().x, 1.1, getDeltaMovement().z);
                hasImpulse = true;
                level.playSound(null, blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.5f, 0.6f);
                slamCooldown = 160;
            }
        }
        if (slamming) {
            slamAirTicks++;
            if (slamAirTicks > 3 && onGround()) {
                slamming = false;
                slam(level);
            }
        }
        tickStrikes(level);
    }

    private void enterPhase(ServerLevel level, int phase) {
        entityData.set(PHASE, phase);
        level.playSound(null, blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.2f, 0.7f);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.5, getZ(), 80, 1.5, 1.5, 1.5, 0.05);
        invulnerableTime = 40;
        if (phase == 2) {
            summon(level, 3);
            summonCooldown = 420;
        }
        if (phase == 3) slamCooldown = 20;
    }

    private void callMortars(ServerLevel level, LivingEntity target, int phase) {
        int n = 3 + phase + random.nextInt(2);
        level.playSound(null, blockPosition(), SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.HOSTILE, 1.5f, 0.5f);
        for (int i = 0; i < n; i++) {
            double ox = i == 0 ? 0 : (random.nextDouble() - 0.5) * 12;
            double oz = i == 0 ? 0 : (random.nextDouble() - 0.5) * 12;
            BlockPos col = BlockPos.containing(target.getX() + ox, target.getY() + 2, target.getZ() + oz);
            int y = col.getY();
            for (int d = 0; d < 6 && level.getBlockState(new BlockPos(col.getX(), y - 1, col.getZ())).isAir(); d++) y--;
            strikes.add(new Strike(new Vec3(col.getX() + 0.5, y, col.getZ() + 0.5), new int[]{30 + i * 4}));
        }
    }

    private void tickStrikes(ServerLevel level) {
        strikes.removeIf(s -> {
            int f = --s.fuse()[0];
            if (f % 3 == 0) {
                for (int a = 0; a < 16; a++) {
                    double ang = a / 16.0 * Mth.TWO_PI;
                    level.sendParticles(MARK, s.pos().x + Math.cos(ang) * 2, s.pos().y + 0.1, s.pos().z + Math.sin(ang) * 2, 1, 0, 0, 0, 0);
                }
            }
            if (f == 12) level.playSound(null, BlockPos.containing(s.pos()), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.HOSTILE, 1.2f, 0.4f);
            if (f <= 0) {
                level.explode(this, s.pos().x, s.pos().y, s.pos().z, 2.6f, false, Level.ExplosionInteraction.NONE);
                level.sendParticles(com.lach.affermath.registry.ModParticles.ASH_FLAKE.get(), s.pos().x, s.pos().y + 1, s.pos().z, 60, 1.5, 1.5, 1.5, 0.08);
                return true;
            }
            return false;
        });
    }

    private void summon(ServerLevel level, int count) {
        for (int i = 0; i < count; i++) {
            HollowSoldier s = ModEntities.HOLLOW_SOLDIER.get().create(level);
            if (s == null) continue;
            double ang = random.nextDouble() * Mth.TWO_PI;
            double x = getX() + Math.cos(ang) * 4, z = getZ() + Math.sin(ang) * 4;
            s.moveTo(x, getY(), z, random.nextFloat() * 360, 0);
            if (!level.noCollision(s)) s.moveTo(getX(), getY(), getZ());
            s.finalizeSpawn(level, level.getCurrentDifficultyAt(s.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            s.setTarget(getTarget());
            level.addFreshEntity(s);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, s.getX(), s.getY() + 1, s.getZ(), 20, 0.3, 0.6, 0.3, 0.02);
        }
        level.playSound(null, blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.5f, 0.5f);
    }

    private void slam(ServerLevel level) {
        level.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2f, 0.5f);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
        level.sendParticles(com.lach.affermath.registry.ModParticles.ASH_FLAKE.get(), getX(), getY() + 0.5, getZ(), 150, 4, 0.5, 4, 0.15);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(blockPosition()).inflate(10), e -> e != this && !(e instanceof HollowSoldier))) {
            Vec3 pull = position().subtract(e.position()).normalize().scale(1.1);
            e.setDeltaMovement(pull.x, 0.45, pull.z);
            e.hurtMarked = true;
            float dmg = (float) Math.max(4, 12 - e.distanceTo(this));
            e.hurt(damageSources().mobAttack(this), dmg);
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), this);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide && random.nextInt(2) == 0) {
            level().addParticle(ParticleTypes.SMOKE, getRandomX(0.6), getY() + 2.2 + random.nextDouble() * 0.3, getRandomZ(0.6), 0, 0.02, 0);
        }
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource src) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return false;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        bossEvent.setName(getDisplayName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (hasCustomName()) bossEvent.setName(getDisplayName());
        entityData.set(PHASE, Math.max(1, tag.getInt("Phase")));
        if (tag.contains("Home")) net.minecraft.nbt.NbtUtils.readBlockPos(tag, "Home").ifPresent(this::setHome);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Phase", getPhase());
        if (home != null) tag.put("Home", net.minecraft.nbt.NbtUtils.writeBlockPos(home));
    }

    @Override
    protected boolean canRide(net.minecraft.world.entity.Entity e) {
        return false;
    }

    @Override
    public boolean canChangeDimensions(Level from, Level to) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.EVOKER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource src) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.55f;
    }
}
