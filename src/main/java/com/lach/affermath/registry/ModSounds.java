package com.lach.affermath.registry;

import com.lach.affermath.Affermath;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Affermath.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> WASTELAND_LOOP = reg("ambient.wasteland.loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> WASTELAND_MOOD = reg("ambient.wasteland.mood");
    public static final DeferredHolder<SoundEvent, SoundEvent> WASTELAND_ADDITIONS = reg("ambient.wasteland.additions");

    public static final DeferredHolder<SoundEvent, SoundEvent> SOLDIER_AMBIENT = reg("entity.hollow_soldier.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOLDIER_HURT = reg("entity.hollow_soldier.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOLDIER_DEATH = reg("entity.hollow_soldier.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> WRAITH_AMBIENT = reg("entity.ash_wraith.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> WRAITH_HURT = reg("entity.ash_wraith.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> WRAITH_DEATH = reg("entity.ash_wraith.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> CROW_AMBIENT = reg("entity.cinder_crow.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> CROW_HURT = reg("entity.cinder_crow.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CROW_DEATH = reg("entity.cinder_crow.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRATOMANCER_AMBIENT = reg("entity.cratomancer.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRATOMANCER_HURT = reg("entity.cratomancer.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRATOMANCER_DEATH = reg("entity.cratomancer.death");

    private static DeferredHolder<SoundEvent, SoundEvent> reg(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Affermath.id(name)));
    }

    private ModSounds() {}
}
