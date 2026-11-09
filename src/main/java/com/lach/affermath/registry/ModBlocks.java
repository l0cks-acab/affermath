package com.lach.affermath.registry;

import com.lach.affermath.Affermath;
import com.lach.affermath.block.AffermathPortalBlock;
import com.lach.affermath.block.AshLayerBlock;
import com.lach.affermath.block.FieldLightBlock;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
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

    private ModBlocks() {}
}
