package com.lach.affermath;

import com.lach.affermath.registry.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Affermath.MODID)
public class Affermath {
    public static final String MODID = "affermath";
    public static final ResourceKey<Level> LEVEL = ResourceKey.create(Registries.DIMENSION, id("affermath"));

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public static boolean isAffermath(Level level) {
        return level != null && level.dimension() == LEVEL;
    }

    public Affermath(IEventBus modBus, ModContainer container) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModItems.COMPONENTS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModParticles.PARTICLES.register(modBus);
        ModEffects.EFFECTS.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModWorld.FEATURES.register(modBus);
        ModWorld.STRUCTURE_TYPES.register(modBus);
        ModWorld.PIECES.register(modBus);
        ModWorld.POI.register(modBus);
        ModTabs.TABS.register(modBus);
        modBus.addListener(ModEntities::attributes);
        modBus.addListener(ModEntities::spawnPlacements);
    }
}
