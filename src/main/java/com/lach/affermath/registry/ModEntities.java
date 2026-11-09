package com.lach.affermath.registry;

import com.lach.affermath.Affermath;
import com.lach.affermath.entity.AshWraith;
import com.lach.affermath.entity.CinderCrow;
import com.lach.affermath.entity.Cratomancer;
import com.lach.affermath.entity.HollowSoldier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, Affermath.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<HollowSoldier>> HOLLOW_SOLDIER = ENTITIES.register("hollow_soldier",
            () -> EntityType.Builder.of(HollowSoldier::new, MobCategory.MONSTER).sized(0.6f, 1.95f).eyeHeight(1.74f)
                    .clientTrackingRange(8).build(key("hollow_soldier")));
    public static final DeferredHolder<EntityType<?>, EntityType<AshWraith>> ASH_WRAITH = ENTITIES.register("ash_wraith",
            () -> EntityType.Builder.of(AshWraith::new, MobCategory.MONSTER).sized(0.6f, 1.8f).eyeHeight(1.6f)
                    .clientTrackingRange(8).build(key("ash_wraith")));
    public static final DeferredHolder<EntityType<?>, EntityType<CinderCrow>> CINDER_CROW = ENTITIES.register("cinder_crow",
            () -> EntityType.Builder.of(CinderCrow::new, MobCategory.AMBIENT).sized(0.5f, 0.6f).eyeHeight(0.45f)
                    .clientTrackingRange(8).build(key("cinder_crow")));
    public static final DeferredHolder<EntityType<?>, EntityType<Cratomancer>> CRATOMANCER = ENTITIES.register("cratomancer",
            () -> EntityType.Builder.of(Cratomancer::new, MobCategory.MONSTER).sized(0.9f, 2.7f).eyeHeight(2.4f)
                    .fireImmune().clientTrackingRange(10).build(key("cratomancer")));

    private static net.minecraft.resources.ResourceKey<EntityType<?>> key(String n) {
        return net.minecraft.resources.ResourceKey.create(Registries.ENTITY_TYPE, Affermath.id(n));
    }

    public static void attributes(EntityAttributeCreationEvent e) {
        e.put(HOLLOW_SOLDIER.get(), HollowSoldier.createAttributes().build());
        e.put(ASH_WRAITH.get(), AshWraith.createAttributes().build());
        e.put(CINDER_CROW.get(), CinderCrow.createAttributes().build());
        e.put(CRATOMANCER.get(), Cratomancer.createAttributes().build());
    }

    public static void spawnPlacements(RegisterSpawnPlacementsEvent e) {
        e.register(HOLLOW_SOLDIER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkAnyLightMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        e.register(ASH_WRAITH.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkAnyLightMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        e.register(CINDER_CROW.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                CinderCrow::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private ModEntities() {}
}
