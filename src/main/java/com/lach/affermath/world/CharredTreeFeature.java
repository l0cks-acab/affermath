package com.lach.affermath.world;

import com.lach.affermath.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Bare, burnt tree: tall trunk with a few crooked branches, sometimes snapped. */
public class CharredTreeFeature extends Feature<NoneFeatureConfiguration> {
    public CharredTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource rand = ctx.random();
        BlockPos o = ctx.origin();
        BlockState ground = level.getBlockState(o.below());
        if (!ground.isSolid() || !level.getBlockState(o).canBeReplaced()) return false;
        if (ground.is(com.lach.affermath.registry.ModBlocks.ASPHALT.get()) || ground.is(com.lach.affermath.registry.ModBlocks.CRACKED_ASPHALT.get())
                || ground.is(com.lach.affermath.registry.ModBlocks.ASPHALT_LINE.get())) return false;
        boolean snapped = rand.nextFloat() < 0.3f;
        int h = snapped ? 2 + rand.nextInt(3) : 5 + rand.nextInt(6);
        BlockState log = ModBlocks.CHARRED_LOG.get().defaultBlockState();
        for (int y = 0; y < h; y++) set(level, o.above(y), log);
        if (snapped) {
            // fallen top lying beside the stump
            Direction d = Direction.Plane.HORIZONTAL.getRandomDirection(rand);
            int len = 3 + rand.nextInt(3);
            BlockState lying = log.setValue(RotatedPillarBlock.AXIS, d.getAxis());
            for (int i = 2; i < 2 + len; i++) {
                BlockPos p = o.relative(d, i);
                if (level.getBlockState(p).canBeReplaced() && level.getBlockState(p.below()).isSolid()) set(level, p, lying);
            }
            return true;
        }
        int branches = 2 + rand.nextInt(4);
        for (int b = 0; b < branches; b++) {
            Direction d = Direction.Plane.HORIZONTAL.getRandomDirection(rand);
            BlockPos p = o.above(h - 1 - rand.nextInt(Math.max(1, h / 2)));
            int len = 2 + rand.nextInt(3);
            for (int i = 0; i < len; i++) {
                p = p.relative(d);
                if (rand.nextFloat() < 0.5f) p = p.above();
                if (rand.nextFloat() < 0.25f) p = p.relative(d.getClockWise());
                if (!level.getBlockState(p).canBeReplaced()) break;
                set(level, p, log.setValue(RotatedPillarBlock.AXIS, d.getAxis()));
            }
        }
        if (rand.nextBoolean()) set(level, o.above(h), log);
        return true;
    }

    private static void set(WorldGenLevel level, BlockPos p, BlockState s) {
        level.setBlock(p, s, 2);
    }
}
