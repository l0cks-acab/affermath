package com.lach.affermath.world;

import com.lach.affermath.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Small debris: rubble heaps, a broken wall stub or a post of rebar. */
public class RubbleFeature extends Feature<NoneFeatureConfiguration> {
    public RubbleFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource rand = ctx.random();
        BlockPos o = ctx.origin();
        if (!level.getBlockState(o.below()).isSolid()) return false;
        int kind = rand.nextInt(10);
        if (kind < 5) heap(level, rand, o);
        else if (kind < 8) wall(level, rand, o);
        else post(level, rand, o);
        return true;
    }

    private void heap(WorldGenLevel level, RandomSource rand, BlockPos o) {
        int r = 1 + rand.nextInt(2);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                int x = o.getX() + dx, z = o.getZ() + dz;
                int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                int h = Math.max(0, r + 1 - Math.abs(dx) - Math.abs(dz) - rand.nextInt(2));
                for (int i = 0; i < h; i++) level.setBlock(new BlockPos(x, y + i, z), debris(rand), 2);
            }
        }
    }

    private void wall(WorldGenLevel level, RandomSource rand, BlockPos o) {
        Direction d = Direction.Plane.HORIZONTAL.getRandomDirection(rand);
        int len = 2 + rand.nextInt(4);
        for (int i = 0; i < len; i++) {
            BlockPos p = o.relative(d, i);
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, p.getX(), p.getZ());
            int h = 1 + rand.nextInt(3);
            for (int j = 0; j < h; j++) {
                BlockState s = rand.nextFloat() < 0.35f ? ModBlocks.CRACKED_SCORCHED_BRICKS.get().defaultBlockState()
                        : ModBlocks.SCORCHED_BRICKS.get().defaultBlockState();
                level.setBlock(new BlockPos(p.getX(), y + j, p.getZ()), s, 2);
            }
        }
    }

    private void post(WorldGenLevel level, RandomSource rand, BlockPos o) {
        level.setBlock(o, ModBlocks.CRACKED_SCORCHED_CONCRETE.get().defaultBlockState(), 2);
        int h = 1 + rand.nextInt(3);
        for (int i = 1; i <= h; i++) level.setBlock(o.above(i), ModBlocks.REBAR.get().defaultBlockState(), 2);
    }

    static BlockState debris(RandomSource rand) {
        return switch (rand.nextInt(7)) {
            case 0, 1, 2 -> ModBlocks.RUBBLE.get().defaultBlockState();
            case 3 -> ModBlocks.CRACKED_SCORCHED_BRICKS.get().defaultBlockState();
            case 4 -> ModBlocks.CRACKED_SCORCHED_CONCRETE.get().defaultBlockState();
            case 5 -> Blocks.COBBLESTONE.defaultBlockState();
            default -> ModBlocks.ASH_BLOCK.get().defaultBlockState();
        };
    }
}
