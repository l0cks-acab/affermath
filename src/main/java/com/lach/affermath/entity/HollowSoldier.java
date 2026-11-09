package com.lach.affermath.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** Burnt husk of a soldier. Moves in squads, never burns, never converts. */
public class HollowSoldier extends Zombie {
    public HollowSoldier(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 26)
                .add(Attributes.ATTACK_DAMAGE, 4)
                .add(Attributes.ARMOR, 4)
                .add(Attributes.FOLLOW_RANGE, 28)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0);
    }

    @Override
    protected void randomizeReinforcementsChance() {
        getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(0);
    }

    @Override
    public void setBaby(boolean baby) {
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public boolean canBreakDoors() {
        return false;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        if (random.nextFloat() < 0.35f) setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        float r = random.nextFloat();
        if (r < 0.25f) setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
        else if (r < 0.4f) setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
    }

    @Override
    public float getVoicePitch() {
        return 0.6f + random.nextFloat() * 0.1f;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return com.lach.affermath.registry.ModSounds.SOLDIER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource src) {
        return com.lach.affermath.registry.ModSounds.SOLDIER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return com.lach.affermath.registry.ModSounds.SOLDIER_DEATH.get();
    }

    @Override
    protected SoundEvent getStepSound() {
        return SoundEvents.HUSK_STEP;
    }
}
