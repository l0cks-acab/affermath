package com.lach.affermath.registry;

import com.lach.affermath.Affermath;
import com.lach.affermath.block.AffermathPortalBlock;
import com.lach.affermath.block.AshLayerBlock;
import com.lach.affermath.block.FieldLightBlock;
import com.lach.affermath.block.ToxicSludgeBlock;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Affermath.MODID);

    private static BlockBehaviour.Properties stone(MapColor c) {
        return BlockBehaviour.Properties.of().mapColor(c).instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops().strength(1.8f, 6f);
    }

    public static final DeferredBlock<AshLayerBlock> ASH_LAYER = BLOCKS.registerBlock("ash_layer", AshLayerBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).replaceable().forceSolidOff()
                    .strength(0.1f).sound(SoundType.SAND).pushReaction(PushReaction.DESTROY)
                    .isViewBlocking((s, l, p) -> s.getValue(SnowLayerBlock.LAYERS) >= 8));
    public static final DeferredBlock<ColoredFallingBlock> ASH_BLOCK = BLOCKS.registerBlock("ash_block", p -> new ColoredFallingBlock(
            new ColorRGBA(0xFF8A8A88), p), () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(0.5f).sound(SoundType.SAND));
    public static final DeferredBlock<Block> ASHEN_SOIL = BLOCKS.registerSimpleBlock("ashen_soil", () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(0.6f).sound(SoundType.GRAVEL));
    public static final DeferredBlock<ColoredFallingBlock> RUBBLE = BLOCKS.registerBlock("rubble", p -> new ColoredFallingBlock(
            new ColorRGBA(0xFF6B6763), p), () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(0.7f).sound(SoundType.GRAVEL));
    public static final DeferredBlock<Block> SCORCHED_BRICKS = BLOCKS.registerSimpleBlock("scorched_bricks", () -> stone(MapColor.COLOR_GRAY));
    public static final DeferredBlock<Block> CRACKED_SCORCHED_BRICKS = BLOCKS.registerSimpleBlock("cracked_scorched_bricks", () -> stone(MapColor.COLOR_GRAY));
    public static final DeferredBlock<Block> SCORCHED_CONCRETE = BLOCKS.registerSimpleBlock("scorched_concrete", () -> stone(MapColor.COLOR_LIGHT_GRAY));
    public static final DeferredBlock<Block> CRACKED_SCORCHED_CONCRETE = BLOCKS.registerSimpleBlock("cracked_scorched_concrete", () -> stone(MapColor.COLOR_LIGHT_GRAY));
    public static final DeferredBlock<RotatedPillarBlock> CHARRED_LOG = BLOCKS.registerBlock("charred_log", RotatedPillarBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(2f).sound(SoundType.WOOD).ignitedByLava());
    public static final DeferredBlock<Block> CHARRED_PLANKS = BLOCKS.registerSimpleBlock("charred_planks", () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(2f, 3f).sound(SoundType.WOOD).ignitedByLava());
    public static final DeferredBlock<IronBarsBlock> REBAR = BLOCKS.registerBlock("rebar", IronBarsBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).requiresCorrectToolForDrops().strength(4f, 6f).sound(SoundType.IRON).noOcclusion());
    public static final DeferredBlock<TransparentBlock> SHATTERED_GLASS = BLOCKS.registerBlock("shattered_glass", TransparentBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(0.3f).sound(SoundType.GLASS).noOcclusion()
                    .isValidSpawn((s, l, p, e) -> false).isRedstoneConductor((s, l, p) -> false)
                    .isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false));
    public static final DeferredBlock<Block> SANDBAGS = BLOCKS.registerSimpleBlock("sandbags", () -> BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(1f).sound(SoundType.WOOL));
    public static final DeferredBlock<AffermathPortalBlock> PORTAL = BLOCKS.registerBlock("affermath_portal", AffermathPortalBlock::new, () -> BlockBehaviour.Properties.of().noCollision().strength(-1f).sound(SoundType.GLASS).lightLevel(s -> 9)
                    .pushReaction(PushReaction.BLOCK).noLootTable());
    public static final DeferredBlock<FieldLightBlock> FIELD_LIGHT = BLOCKS.registerBlock("field_light", FieldLightBlock::new, () -> BlockBehaviour.Properties.of().replaceable().noCollision().noLootTable().air().lightLevel(s -> 13).instabreak());

    // ---- wasteland
    public static final DeferredBlock<Block> RUSTED_PLATING = BLOCKS.registerSimpleBlock("rusted_plating", () ->
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).requiresCorrectToolForDrops().strength(3f, 6f).sound(SoundType.METAL));
    public static final DeferredBlock<ColoredFallingBlock> SCRAP_PILE = BLOCKS.registerBlock("scrap_pile", p -> new ColoredFallingBlock(
            new ColorRGBA(0xFF6E4A33), p), () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(0.8f).sound(SoundType.CHAIN));
    public static final DeferredBlock<RotatedPillarBlock> BURNT_TIRE = BLOCKS.registerBlock("burnt_tire", RotatedPillarBlock::new, () ->
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(1f).sound(SoundType.WOOL).ignitedByLava());
    public static final DeferredBlock<ToxicSludgeBlock> TOXIC_SLUDGE = BLOCKS.registerBlock("toxic_sludge", ToxicSludgeBlock::new, () ->
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GREEN).replaceable().noCollision().strength(0.2f)
                    .sound(SoundType.SLIME_BLOCK).lightLevel(s -> 4).noOcclusion().pushReaction(PushReaction.DESTROY)
                    .isValidSpawn((s, l, p, e) -> false));


    // ---- building blocks
    public static final DeferredBlock<StairBlock> SCORCHED_BRICK_STAIRS = BLOCKS.registerBlock("scorched_brick_stairs", p -> new StairBlock(SCORCHED_BRICKS.get().defaultBlockState(), p), () -> BlockBehaviour.Properties.ofFullCopy(SCORCHED_BRICKS.get()));
    public static final DeferredBlock<SlabBlock> SCORCHED_BRICK_SLAB = BLOCKS.registerBlock("scorched_brick_slab", p -> new SlabBlock(p), () -> BlockBehaviour.Properties.ofFullCopy(SCORCHED_BRICKS.get()));
    public static final DeferredBlock<WallBlock> SCORCHED_BRICK_WALL = BLOCKS.registerBlock("scorched_brick_wall", p -> new WallBlock(p), () -> BlockBehaviour.Properties.ofFullCopy(SCORCHED_BRICKS.get()).forceSolidOn());
    public static final DeferredBlock<StairBlock> CRACKED_SCORCHED_BRICK_STAIRS = BLOCKS.registerBlock("cracked_scorched_brick_stairs", p -> new StairBlock(CRACKED_SCORCHED_BRICKS.get().defaultBlockState(), p), () -> BlockBehaviour.Properties.ofFullCopy(CRACKED_SCORCHED_BRICKS.get()));
    public static final DeferredBlock<SlabBlock> CRACKED_SCORCHED_BRICK_SLAB = BLOCKS.registerBlock("cracked_scorched_brick_slab", p -> new SlabBlock(p), () -> BlockBehaviour.Properties.ofFullCopy(CRACKED_SCORCHED_BRICKS.get()));
    public static final DeferredBlock<WallBlock> CRACKED_SCORCHED_BRICK_WALL = BLOCKS.registerBlock("cracked_scorched_brick_wall", p -> new WallBlock(p), () -> BlockBehaviour.Properties.ofFullCopy(CRACKED_SCORCHED_BRICKS.get()).forceSolidOn());
    public static final DeferredBlock<StairBlock> SCORCHED_CONCRETE_STAIRS = BLOCKS.registerBlock("scorched_concrete_stairs", p -> new StairBlock(SCORCHED_CONCRETE.get().defaultBlockState(), p), () -> BlockBehaviour.Properties.ofFullCopy(SCORCHED_CONCRETE.get()));
    public static final DeferredBlock<SlabBlock> SCORCHED_CONCRETE_SLAB = BLOCKS.registerBlock("scorched_concrete_slab", p -> new SlabBlock(p), () -> BlockBehaviour.Properties.ofFullCopy(SCORCHED_CONCRETE.get()));
    public static final DeferredBlock<WallBlock> SCORCHED_CONCRETE_WALL = BLOCKS.registerBlock("scorched_concrete_wall", p -> new WallBlock(p), () -> BlockBehaviour.Properties.ofFullCopy(SCORCHED_CONCRETE.get()).forceSolidOn());
    public static final DeferredBlock<StairBlock> CRACKED_SCORCHED_CONCRETE_STAIRS = BLOCKS.registerBlock("cracked_scorched_concrete_stairs", p -> new StairBlock(CRACKED_SCORCHED_CONCRETE.get().defaultBlockState(), p), () -> BlockBehaviour.Properties.ofFullCopy(CRACKED_SCORCHED_CONCRETE.get()));
    public static final DeferredBlock<SlabBlock> CRACKED_SCORCHED_CONCRETE_SLAB = BLOCKS.registerBlock("cracked_scorched_concrete_slab", p -> new SlabBlock(p), () -> BlockBehaviour.Properties.ofFullCopy(CRACKED_SCORCHED_CONCRETE.get()));
    public static final DeferredBlock<WallBlock> CRACKED_SCORCHED_CONCRETE_WALL = BLOCKS.registerBlock("cracked_scorched_concrete_wall", p -> new WallBlock(p), () -> BlockBehaviour.Properties.ofFullCopy(CRACKED_SCORCHED_CONCRETE.get()).forceSolidOn());
    public static final DeferredBlock<StairBlock> CHARRED_STAIRS = BLOCKS.registerBlock("charred_stairs", p -> new StairBlock(CHARRED_PLANKS.get().defaultBlockState(), p), () -> BlockBehaviour.Properties.ofFullCopy(CHARRED_PLANKS.get()));
    public static final DeferredBlock<SlabBlock> CHARRED_SLAB = BLOCKS.registerBlock("charred_slab", p -> new SlabBlock(p), () -> BlockBehaviour.Properties.ofFullCopy(CHARRED_PLANKS.get()));
    public static final DeferredBlock<FenceBlock> CHARRED_FENCE = BLOCKS.registerBlock("charred_fence", p -> new FenceBlock(p), () -> BlockBehaviour.Properties.ofFullCopy(CHARRED_PLANKS.get()));
    public static final DeferredBlock<FenceGateBlock> CHARRED_FENCE_GATE = BLOCKS.registerBlock("charred_fence_gate", p -> new FenceGateBlock(WoodType.CRIMSON, p), () -> BlockBehaviour.Properties.ofFullCopy(CHARRED_PLANKS.get()).forceSolidOn());
    public static final DeferredBlock<DoorBlock> CHARRED_DOOR = BLOCKS.registerBlock("charred_door", p -> new DoorBlock(BlockSetType.CRIMSON, p), () -> BlockBehaviour.Properties.ofFullCopy(CHARRED_PLANKS.get()).noOcclusion().strength(3f).pushReaction(PushReaction.DESTROY));
    public static final DeferredBlock<TrapDoorBlock> CHARRED_TRAPDOOR = BLOCKS.registerBlock("charred_trapdoor", p -> new TrapDoorBlock(BlockSetType.CRIMSON, p), () -> BlockBehaviour.Properties.ofFullCopy(CHARRED_PLANKS.get()).noOcclusion().strength(3f).isValidSpawn((s, l, q, e) -> false));

    private ModBlocks() {}
}
