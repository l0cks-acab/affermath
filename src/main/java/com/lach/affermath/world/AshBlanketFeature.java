package com.lach.affermath.world;

import com.lach.affermath.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

/** Settles 1-4 layers of ash over every open surface in the chunk, drifting smoothly. */
public class AshBlanketFeature extends Feature<NoneFeatureConfiguration> {
    public AshBlanketFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos o = ctx.origin();
        long seed = level.getSeed();
        int cx = o.getX() & ~15, cz = o.getZ() & ~15;
        BlockState ash = ModBlocks.ASH_LAYER.get().defaultBlockState();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = cx + dx, z = cz + dz;
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                m.set(x, y, z);
                if (!level.getBlockState(m).isAir()) continue;
                if (!level.getFluidState(m.below()).is(Fluids.EMPTY)) continue;
                if (!ash.canSurvive(level, m)) continue;
                double n = noise(seed, x / 9.0, z / 9.0) * 0.7 + noise(seed + 7, x / 3.0, z / 3.0) * 0.3;
                int layers = Mth.clamp(1 + (int) (n * 4), 1, 4);
                level.setBlock(m, ash.setValue(SnowLayerBlock.LAYERS, layers), 2);
            }
        }
        return true;
    }

    private static double hash(long seed, int x, int z) {
        long h = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (z * 0xC2B2AE3D27D4EB4FL);
        h = (h ^ (h >>> 31)) * 0xBF58476D1CE4E5B9L;
        h ^= h >>> 29;
        return (h & 0xFFFFFF) / (double) 0x1000000;
    }

    static double noise(long seed, double x, double z) {
        int x0 = Mth.floor(x), z0 = Mth.floor(z);
        double fx = x - x0, fz = z - z0;
        fx = fx * fx * (3 - 2 * fx);
        fz = fz * fz * (3 - 2 * fz);
        double a = hash(seed, x0, z0), b = hash(seed, x0 + 1, z0), c = hash(seed, x0, z0 + 1), d = hash(seed, x0 + 1, z0 + 1);
        return Mth.lerp(fz, Mth.lerp(fx, a, b), Mth.lerp(fx, c, d));
    }
}
