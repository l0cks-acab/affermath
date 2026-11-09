package com.lach.affermath.world;

import com.lach.affermath.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Post-war wasteland clutter: scrap heaps, burnt-out car wrecks, toppled radio masts and contaminated puddles. */
public final class WastelandFeatures {
    private WastelandFeatures() {}

    static int ground(WorldGenLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
    }

    static boolean solidBelow(WorldGenLevel level, BlockPos p) {
        return level.getBlockState(p.below()).isSolid() && level.getFluidState(p.below()).isEmpty();
    }

    static void set(WorldGenLevel level, BlockPos p, BlockState s) {
        level.setBlock(p, s, 2);
    }

    static BlockState ash(int layers) {
        return ModBlocks.ASH_LAYER.get().defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers);
    }

    // ------------------------------------------------------------------ scrap heap
    public static class ScrapHeap extends Feature<NoneFeatureConfiguration> {
        public ScrapHeap(Codec<NoneFeatureConfiguration> c) {
            super(c);
        }

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
            WorldGenLevel level = ctx.level();
            RandomSource r = ctx.random();
            BlockPos o = ctx.origin();
            if (!solidBelow(level, o)) return false;
            int rad = 2 + r.nextInt(2);
            for (int dx = -rad; dx <= rad; dx++) {
                for (int dz = -rad; dz <= rad; dz++) {
                    double d = Math.sqrt(dx * dx + dz * dz) + r.nextDouble() * 0.8;
                    int h = (int) Math.round(rad + 0.6 - d);
                    if (h <= 0) continue;
                    int x = o.getX() + dx, z = o.getZ() + dz, y = ground(level, x, z);
                    for (int i = 0; i < h; i++) set(level, new BlockPos(x, y + i, z), piece(r, i == h - 1));
                    if (r.nextInt(3) == 0) set(level, new BlockPos(x, y + h, z), ash(1 + r.nextInt(2)));
                }
            }
            for (int i = 0, n = 1 + r.nextInt(3); i < n; i++) { // rebar and girders poking out
                int x = o.getX() + r.nextInt(rad * 2 + 1) - rad, z = o.getZ() + r.nextInt(rad * 2 + 1) - rad, y = ground(level, x, z);
                for (int k = 0, len = 1 + r.nextInt(3); k < len; k++) set(level, new BlockPos(x, y + k, z), ModBlocks.REBAR.get().defaultBlockState());
            }
            return true;
        }

        static BlockState piece(RandomSource r, boolean top) {
            int k = r.nextInt(20);
            if (k < 11) return ModBlocks.SCRAP_PILE.get().defaultBlockState();
            if (k < 14) return ModBlocks.RUSTED_PLATING.get().defaultBlockState();
            if (k < 16) return ModBlocks.RUBBLE.get().defaultBlockState();
            if (k < 18 && top) return ModBlocks.BURNT_TIRE.get().defaultBlockState()
                    .setValue(RotatedPillarBlock.AXIS, Direction.Axis.values()[r.nextInt(3)]);
            return ModBlocks.SCRAP_PILE.get().defaultBlockState();
        }
    }

    // ------------------------------------------------------------------ car wreck
    public static class CarWreck extends Feature<NoneFeatureConfiguration> {
        public CarWreck(Codec<NoneFeatureConfiguration> c) {
            super(c);
        }

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
            WorldGenLevel level = ctx.level();
            RandomSource r = ctx.random();
            BlockPos o = ctx.origin();
            if (!solidBelow(level, o)) return false;
            Direction fwd = Direction.Plane.HORIZONTAL.getRandomDirection(r);
            Direction side = fwd.getClockWise();
            // keep it on reasonably flat ground
            int base = ground(level, o.getX(), o.getZ());
            for (int i = -2; i <= 2; i++) {
                for (int j = -1; j <= 1; j++) {
                    BlockPos p = o.relative(fwd, i).relative(side, j);
                    if (Math.abs(ground(level, p.getX(), p.getZ()) - base) > 1) return false;
                }
            }
            int y0 = base - (r.nextFloat() < 0.35f ? 1 : 0); // half-sunk in the ash
            boolean overturned = r.nextFloat() < 0.15f;
            BlockState plate = ModBlocks.RUSTED_PLATING.get().defaultBlockState();
            BlockState glass = ModBlocks.SHATTERED_GLASS.get().defaultBlockState();
            BlockState tire = ModBlocks.BURNT_TIRE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, side.getAxis());
            int wheelY = overturned ? 3 : 0, chassisY = overturned ? 2 : 1, cabinY = overturned ? 1 : 2, roofY = overturned ? 0 : 3;
            for (int i = -2; i <= 2; i++) {
                for (int j = -1; j <= 1; j++) {
                    BlockPos col = o.relative(fwd, i).relative(side, j).atY(y0);
                    for (int k = 0; k <= 3; k++) set(level, col.above(k), Blocks.AIR.defaultBlockState());
                    boolean wheel = (i == -2 || i == 1) && j != 0;
                    if (wheel && r.nextInt(5) != 0) set(level, col.above(wheelY), tire);
                    if (r.nextInt(9) != 0) set(level, col.above(chassisY), plate);
                    boolean cabin = i >= -1 && i <= 0;
                    if (cabin || i == 1) {
                        if (j != 0) set(level, col.above(cabinY), r.nextInt(3) == 0 ? glass : r.nextInt(6) == 0 ? plate : Blocks.AIR.defaultBlockState());
                        else if (i == 1) set(level, col.above(cabinY), r.nextInt(2) == 0 ? glass : Blocks.AIR.defaultBlockState()); // windscreen
                    }
                    if (cabin && r.nextInt(4) != 0) set(level, col.above(roofY), plate);
                }
            }
            // ash settled on the roof and bonnet, scrap fallen beside it
            for (int i = -2; i <= 2; i++) {
                for (int j = -2; j <= 2; j++) {
                    BlockPos col = o.relative(fwd, i).relative(side, j);
                    int top = ground(level, col.getX(), col.getZ());
                    BlockPos t = new BlockPos(col.getX(), top, col.getZ());
                    if (level.getBlockState(t).isAir() && solidBelow(level, t) && r.nextInt(3) == 0) set(level, t, ash(1 + r.nextInt(2)));
                    else if (Math.abs(j) == 2 && level.getBlockState(t).isAir() && solidBelow(level, t) && r.nextInt(6) == 0)
                        set(level, t, ModBlocks.SCRAP_PILE.get().defaultBlockState());
                }
            }
            return true;
        }
    }

    // ------------------------------------------------------------------ radio mast
    public static class RadioMast extends Feature<NoneFeatureConfiguration> {
        public RadioMast(Codec<NoneFeatureConfiguration> c) {
            super(c);
        }

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
            WorldGenLevel level = ctx.level();
            RandomSource r = ctx.random();
            BlockPos o = ctx.origin();
            if (!solidBelow(level, o)) return false;
            int y0 = ground(level, o.getX(), o.getZ());
            BlockState concrete = ModBlocks.SCORCHED_CONCRETE.get().defaultBlockState();
            BlockState cracked = ModBlocks.CRACKED_SCORCHED_CONCRETE.get().defaultBlockState();
            BlockState bar = ModBlocks.REBAR.get().defaultBlockState();
            BlockState plate = ModBlocks.RUSTED_PLATING.get().defaultBlockState();
            for (int dx = -1; dx <= 2; dx++) {
                for (int dz = -1; dz <= 2; dz++) {
                    BlockPos p = new BlockPos(o.getX() + dx, y0 - 1, o.getZ() + dz);
                    set(level, p, r.nextInt(3) == 0 ? cracked : concrete);
                    for (int k = 0; k < 2; k++) set(level, new BlockPos(p.getX(), y0 - 2 - k, p.getZ()), concrete);
                }
            }
            int height = 12 + r.nextInt(9);
            boolean collapsed = r.nextFloat() < 0.65f;
            int standing = collapsed ? height / 3 + r.nextInt(height / 3) : height;
            for (int k = 0; k < standing; k++) {
                boolean brace = k % 4 == 3;
                for (int dx = 0; dx <= 1; dx++) {
                    for (int dz = 0; dz <= 1; dz++) set(level, new BlockPos(o.getX() + dx, y0 + k, o.getZ() + dz), brace ? plate : bar);
                }
            }
            if (!collapsed) {
                BlockPos top = new BlockPos(o.getX(), y0 + standing, o.getZ());
                set(level, top, plate);
                for (int k = 1; k <= 3; k++) set(level, top.above(k), bar); // aerial
                return true;
            }
            // the snapped top lies across the ground, buckled
            Direction d = Direction.Plane.HORIZONTAL.getRandomDirection(r);
            Direction s = d.getClockWise();
            BlockPos start = new BlockPos(o.getX(), y0, o.getZ()).relative(d, 2);
            int len = height - standing;
            for (int t = 0; t < len; t++) {
                for (int w = 0; w <= 1; w++) {
                    BlockPos c = start.relative(d, t).relative(s, w + (t > len / 2 ? 1 : 0));
                    int gy = ground(level, c.getX(), c.getZ());
                    BlockPos p = new BlockPos(c.getX(), gy, c.getZ());
                    if (!solidBelow(level, p)) continue;
                    set(level, p, t % 4 == 3 ? plate : bar);
                }
            }
            return true;
        }
    }

    // ------------------------------------------------------------------ toxic puddle
    public static class ToxicPuddle extends Feature<NoneFeatureConfiguration> {
        public ToxicPuddle(Codec<NoneFeatureConfiguration> c) {
            super(c);
        }

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
            WorldGenLevel level = ctx.level();
            RandomSource r = ctx.random();
            BlockPos o = ctx.origin();
            if (!solidBelow(level, o)) return false;
            int rad = 2 + r.nextInt(3);
            double ox = r.nextDouble() * 6, oz = r.nextDouble() * 6;
            BlockState sludge = ModBlocks.TOXIC_SLUDGE.get().defaultBlockState();
            BlockState soil = ModBlocks.ASHEN_SOIL.get().defaultBlockState();
            int placed = 0;
            for (int dx = -rad - 1; dx <= rad + 1; dx++) {
                for (int dz = -rad - 1; dz <= rad + 1; dz++) {
                    double wob = Mth.sin((float) (dx * 0.9 + ox)) * 0.6 + Mth.cos((float) (dz * 0.8 + oz)) * 0.6;
                    double d = Math.sqrt(dx * dx + dz * dz) + wob;
                    if (d > rad) continue;
                    int x = o.getX() + dx, z = o.getZ() + dz;
                    int top = ground(level, x, z) - 1;
                    BlockPos surf = new BlockPos(x, top, z);
                    if (!level.getBlockState(surf).isSolid()) continue;
                    boolean deep = d < rad - 1.2;
                    BlockPos floor = deep ? surf.below() : surf;
                    // the pool needs walls: only sink the middle where every neighbour is still solid
                    set(level, floor, soil);
                    if (deep) set(level, surf, Blocks.AIR.defaultBlockState());
                    BlockPos at = floor.above();
                    set(level, at, sludge);
                    set(level, at.above(), Blocks.AIR.defaultBlockState());
                    placed++;
                }
            }
            return placed > 0;
        }
    }
}
