package com.lach.affermath.world;

import com.lach.affermath.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Old highways: a loose grid of meandering two-lane roads, worked out per column from the world
 * seed so they run on seamlessly across chunks. Cracked asphalt, potholes, crumbling verges and a
 * faded centre line, with burnt-out cars abandoned along them.
 */
public class RoadFeature extends Feature<NoneFeatureConfiguration> {
    static final int SPACING = 224, HALF = 2, WOBBLE = 70;

    public RoadFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    /** Signed distance from a column to the centre of the nearest road running along the given axis. */
    static double offset(long seed, int across, int along, boolean nsRoad) {
        int cell = Math.floorDiv(across + SPACING / 2, SPACING);
        long s = seed ^ (nsRoad ? 0x5EEDL : 0xA5A5L) ^ ((long) cell * 0x9E3779B97F4A7C15L);
        double wob = (AshBlanketFeature.noise(s, along / 110.0, cell * 3.1) - 0.5) * 2 * WOBBLE
                + (AshBlanketFeature.noise(s + 1, along / 37.0, cell * 1.7) - 0.5) * 8;
        return across - (cell * SPACING + wob);
    }

    /** Whether this road segment survived the war: some stretches are simply gone. */
    static boolean exists(long seed, int across, int along, boolean nsRoad) {
        int cell = Math.floorDiv(across + SPACING / 2, SPACING);
        return AshBlanketFeature.noise(seed ^ (nsRoad ? 0x77L : 0x99L) ^ ((long) cell << 20), along / 160.0, cell * 0.7) > 0.22;
    }

    /** Mean ground height along the road's centre line around this column (same for every column across the road). */
    static int graded(WorldGenLevel level, long seed, int x, int z, boolean nsRoad) {
        int along = nsRoad ? z : x, across = nsRoad ? x : z;
        int sum = 0, n = 0;
        for (int k = -8; k <= 8; k += 4) {
            int a = along + k;
            int c = (int) Math.round(across - offset(seed, across, a, nsRoad));
            sum += nsRoad ? WastelandFeatures.ground(level, c, a) : WastelandFeatures.ground(level, a, c);
            n++;
        }
        return Math.round(sum / (float) n);
    }

    static boolean natural(BlockState s) {
        return s.is(ModBlocks.ASH_BLOCK.get()) || s.is(ModBlocks.ASHEN_SOIL.get()) || s.is(ModBlocks.RUBBLE.get());
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource r = ctx.random();
        long seed = level.getSeed();
        BlockPos o = ctx.origin();
        int cx = o.getX() & ~15, cz = o.getZ() & ~15;
        BlockState asphalt = ModBlocks.ASPHALT.get().defaultBlockState();
        BlockState cracked = ModBlocks.CRACKED_ASPHALT.get().defaultBlockState();
        BlockState line = ModBlocks.ASPHALT_LINE.get().defaultBlockState();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        BlockPos carNs = null, carEw = null;
        int nsCount = 0, ewCount = 0;
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = cx + dx, z = cz + dz;
                double ns = offset(seed, x, z, true), ew = offset(seed, z, x, false);
                boolean onNs = Math.abs(ns) <= HALF + 0.5 && exists(seed, x, z, true);
                boolean onEw = Math.abs(ew) <= HALF + 0.5 && exists(seed, z, x, false);
                if (!onNs && !onEw) continue;
                int gy = WastelandFeatures.ground(level, x, z);
                m.set(x, gy - 1, z);
                if (!natural(level.getBlockState(m))) continue;
                // grade: the road holds a smoothed level, cutting through bumps and filling dips
                int y = graded(level, seed, x, z, onNs) - 1;
                if (Math.abs(y + 1 - gy) <= 5) {
                    for (int k = y + 1; k < gy + 2; k++) level.setBlock(m.set(x, k, z), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
                    for (int k = gy - 1; k < y; k++) level.setBlock(m.set(x, k, z), ModBlocks.ASHEN_SOIL.get().defaultBlockState(), 2);
                } else {
                    y = gy - 1;
                }
                m.set(x, y, z);
                double d = onNs ? Math.abs(ns) : Math.abs(ew);
                boolean edge = d > HALF - 0.5;
                if (edge && r.nextInt(3) == 0) continue; // crumbled verge
                BlockState s;
                if (onNs && onEw) {
                    s = r.nextInt(3) == 0 ? cracked : asphalt; // junctions have no lines
                } else if (d < 0.5 && Math.floorMod(onNs ? z : x, 6) < 3 && r.nextInt(5) != 0) {
                    s = line.setValue(RotatedPillarBlock.AXIS, onNs ? Direction.Axis.Z : Direction.Axis.X);
                } else {
                    s = r.nextInt(4) == 0 ? cracked : asphalt;
                }
                if (r.nextInt(45) == 0) {
                    // pothole: a dip of rubble and ash
                    level.setBlock(m, ModBlocks.RUBBLE.get().defaultBlockState(), 2);
                    continue;
                }
                level.setBlock(m, s, 2);
                if (d < 0.5) {
                    if (onNs && !onEw) { nsCount++; if (r.nextInt(nsCount) == 0) carNs = m.immutable(); }
                    if (onEw && !onNs) { ewCount++; if (r.nextInt(ewCount) == 0) carEw = m.immutable(); }
                }
            }
        }
        // abandoned traffic: cars sit in a lane, nose along the road, now and then a little pile-up
        tryCar(level, r, carNs, Direction.SOUTH);
        tryCar(level, r, carEw, Direction.EAST);
        // roadside ruins
        if (r.nextFloat() < 0.2f) {
            boolean useNs = carNs != null && (carEw == null || r.nextBoolean());
            BlockPos c = useNs ? carNs : carEw;
            if (c != null) {
                Direction front = useNs ? (r.nextBoolean() ? Direction.WEST : Direction.EAST) : (r.nextBoolean() ? Direction.NORTH : Direction.SOUTH);
                BlockPos origin = c.relative(front.getOpposite(), HALF + 2 + r.nextInt(3));
                // shift back along the road so the building is roughly centred on the sample point
                Direction right = front.getCounterClockWise();
                origin = origin.relative(right, -4);
                if (clear(seed, origin, front, 12, 10)) {
                    int k = r.nextInt(100);
                    if (k < 40) Buildings.apartment(level, r, origin, front);
                    else if (k < 75) Buildings.shop(level, r, origin, front);
                    else Buildings.shack(level, r, origin, front);
                }
            }
        }
        return true;
    }

    static boolean onAnyRoad(long seed, int x, int z) {
        return (Math.abs(offset(seed, x, z, true)) <= HALF + 1 && exists(seed, x, z, true))
                || (Math.abs(offset(seed, z, x, false)) <= HALF + 1 && exists(seed, z, x, false));
    }

    /** Keeps buildings off every road, including the cross street at a junction. */
    static boolean clear(long seed, BlockPos origin, Direction front, int w, int d) {
        Direction right = front.getCounterClockWise();
        for (int u = -1; u <= w; u += 2) {
            for (int v = 0; v <= d; v += 2) {
                BlockPos p = origin.relative(right, u).relative(front, -v);
                if (onAnyRoad(seed, p.getX(), p.getZ())) return false;
            }
        }
        return true;
    }

    private static void tryCar(WorldGenLevel level, RandomSource r, BlockPos centre, Direction along) {
        if (centre == null || r.nextFloat() > 0.3f) return;
        Direction fwd = r.nextBoolean() ? along : along.getOpposite();
        Direction lane = fwd.getClockWise(); // drive on the right
        BlockPos p = centre.above().relative(lane, 1 + r.nextInt(2) - 1 + (r.nextInt(4) == 0 ? -2 : 0));
        int skew = r.nextInt(6) == 0 ? 1 : 0;
        WastelandFeatures.CarWreck.build(level, r, p, skew == 1 ? lane : fwd, false);
        if (r.nextFloat() < 0.2f) {
            BlockPos q = p.relative(fwd, 6 + r.nextInt(2)).relative(lane.getOpposite(), r.nextInt(2));
            q = new BlockPos(q.getX(), WastelandFeatures.ground(level, q.getX(), q.getZ()), q.getZ());
            WastelandFeatures.CarWreck.build(level, r, q, fwd, false);
        }
    }
}
