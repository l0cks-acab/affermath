package com.lach.affermath.world;

import com.lach.affermath.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Shell crater: a scorched bowl with a raised rubble rim. */
public class CraterFeature extends Feature<NoneFeatureConfiguration> {
    public CraterFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource rand = ctx.random();
        BlockPos o = ctx.origin();
        int r = 4 + rand.nextInt(8);
        double depth = r * (0.45 + rand.nextDouble() * 0.2);
        int baseY = o.getY();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int dx = -r - 2; dx <= r + 2; dx++) {
            for (int dz = -r - 2; dz <= r + 2; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz) + rand.nextDouble() * 0.6;
                int x = o.getX() + dx, z = o.getZ() + dz;
                int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                if (Math.abs(top - baseY) > r) continue;
                if (dist <= r) {
                    double t = dist / r;
                    int bottom = (int) Math.round(baseY - depth * (1 - t * t));
                    for (int y = bottom + 1; y <= top + 2; y++) {
                        m.set(x, y, z);
                        BlockState s = level.getBlockState(m);
                        if (!s.isAir() && !s.is(Blocks.BEDROCK)) level.setBlock(m, Blocks.AIR.defaultBlockState(), 2);
                    }
                    m.set(x, bottom, z);
                    level.setBlock(m, floor(rand, t), 2);
                    m.set(x, bottom - 1, z);
                    if (rand.nextFloat() < 0.6f) level.setBlock(m, ModBlocks.RUBBLE.get().defaultBlockState(), 2);
                } else if (dist <= r + 2 && rand.nextFloat() < 0.8f) {
                    int h = dist <= r + 1 ? 1 + rand.nextInt(2) : rand.nextInt(2);
                    for (int i = 1; i <= h; i++) {
                        m.set(x, top + i, z);
                        if (level.getBlockState(m).isAir())
                            level.setBlock(m, rand.nextBoolean() ? ModBlocks.RUBBLE.get().defaultBlockState() : ModBlocks.ASHEN_SOIL.get().defaultBlockState(), 2);
                    }
                }
            }
        }
        return true;
    }

    private static BlockState floor(RandomSource rand, double t) {
        float f = rand.nextFloat();
        if (t < 0.4 && f < 0.35f) return Blocks.BLACKSTONE.defaultBlockState();
        if (f < 0.25f) return ModBlocks.ASH_BLOCK.get().defaultBlockState();
        if (f < 0.55f) return ModBlocks.RUBBLE.get().defaultBlockState();
        if (f < 0.65f) return Blocks.COAL_ORE.defaultBlockState();
        return ModBlocks.ASHEN_SOIL.get().defaultBlockState();
    }
}
