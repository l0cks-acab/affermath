package com.lach.affermath.registry;

import com.google.common.collect.ImmutableSet;
import com.lach.affermath.Affermath;
import com.lach.affermath.world.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModWorld {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Affermath.MODID);
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, Affermath.MODID);
    public static final DeferredRegister<StructurePieceType> PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, Affermath.MODID);
    public static final DeferredRegister<PoiType> POI = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Affermath.MODID);

    public static final DeferredHolder<Feature<?>, CraterFeature> CRATER = FEATURES.register("crater", () -> new CraterFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, CharredTreeFeature> CHARRED_TREE = FEATURES.register("charred_tree", () -> new CharredTreeFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, RubbleFeature> RUBBLE = FEATURES.register("rubble_pile", () -> new RubbleFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, AshBlanketFeature> ASH_BLANKET = FEATURES.register("ash_blanket", () -> new AshBlanketFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<StructureType<?>, StructureType<RuinStructure>> RUIN = STRUCTURE_TYPES.register("ruin", () -> () -> RuinStructure.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<BunkerStructure>> BUNKER = STRUCTURE_TYPES.register("bunker", () -> () -> BunkerStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> RUIN_PIECE = PIECES.register("ruin",
            () -> (StructurePieceType.ContextlessType) RuinStructure.Piece::new);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> BUNKER_PIECE = PIECES.register("bunker",
            () -> (StructurePieceType.ContextlessType) BunkerStructure.Piece::new);

    public static final DeferredHolder<PoiType, PoiType> PORTAL_POI = POI.register("affermath_portal",
            () -> new PoiType(ImmutableSet.copyOf(ModBlocks.PORTAL.get().getStateDefinition().getPossibleStates()), 0, 1));

    private ModWorld() {}
}
