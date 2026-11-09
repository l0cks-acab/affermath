package com.lach.affermath.world;

import com.lach.affermath.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.ArrayList;
import java.util.List;

/**
 * Roadside ruins, built procedurally so no two match: gutted concrete apartment blocks with
 * balconies and wall units, two-storey brick shopfronts, and patchwork scrap shacks up on stilts.
 * Local coordinates: u runs along the road, v runs away from it (v = 0 is the wall facing the road).
 */
public final class Buildings {
    private Buildings() {}

    static final class Site {
        final WorldGenLevel level;
        final RandomSource r;
        final BlockPos origin;
        final Direction front, right;
        final List<BlockPos> connect = new ArrayList<>();

        Site(WorldGenLevel level, RandomSource r, BlockPos origin, Direction front) {
            this.level = level;
            this.r = r;
            this.origin = origin;
            this.front = front;
            this.right = front.getCounterClockWise();
        }

        BlockPos at(int u, int y, int v) {
            return origin.relative(right, u).relative(front, -v).above(y);
        }

        void set(int u, int y, int v, BlockState s) {
            BlockPos p = at(u, y, v);
            level.setBlock(p, s, 2);
            if (s.is(ModBlocks.REBAR.get()) || s.getBlock() instanceof net.minecraft.world.level.block.WallBlock
                    || s.is(Blocks.IRON_BARS)) connect.add(p);
        }

        BlockState get(int u, int y, int v) {
            return level.getBlockState(at(u, y, v));
        }

        void air(int u, int y, int v) {
            set(u, y, v, Blocks.AIR.defaultBlockState());
        }

        /** Fixes up walls and bars so they join their neighbours. */
        void finish() {
            for (BlockPos p : connect) {
                BlockState s = level.getBlockState(p);
                if (!s.isAir()) level.setBlock(p, Block.updateFromNeighbourShapes(s, level, p), 2);
            }
        }

        void chest(int u, int y, int v) {
            BlockPos p = at(u, y, v);
            level.setBlock(p, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, front), 2);
            RandomizableContainer.setBlockEntityLootTable(level, r, p, Loot.RUIN);
        }
    }

    // ------------------------------------------------------------------ palette
    static BlockState concrete(RandomSource r) {
        return (r.nextInt(3) == 0 ? ModBlocks.CRACKED_SCORCHED_CONCRETE : ModBlocks.SCORCHED_CONCRETE).get().defaultBlockState();
    }

    static BlockState brick(RandomSource r) {
        return (r.nextInt(3) == 0 ? ModBlocks.CRACKED_SCORCHED_BRICKS : ModBlocks.SCORCHED_BRICKS).get().defaultBlockState();
    }

    static BlockState window(RandomSource r) {
        int k = r.nextInt(6);
        if (k < 2) return ModBlocks.SHATTERED_GLASS.get().defaultBlockState();
        if (k == 2) return Blocks.IRON_BARS.defaultBlockState();
        return Blocks.AIR.defaultBlockState();
    }

    /** Lowest and highest ground under a footprint, or null when it is too steep to build on. */
    static int[] groundRange(Site s, int w, int d) {
        int lo = Integer.MAX_VALUE, hi = Integer.MIN_VALUE;
        for (int u = 0; u < w; u++) {
            for (int v = 0; v < d; v++) {
                BlockPos p = s.at(u, 0, v);
                int g = WastelandFeatures.ground(s.level, p.getX(), p.getZ());
                lo = Math.min(lo, g);
                hi = Math.max(hi, g);
            }
        }
        return hi - lo > 4 ? null : new int[]{lo, hi};
    }

    /** Solid footing from the floor down into the ground so nothing floats on slopes. */
    static void footing(Site s, int w, int d, BlockState fill) {
        for (int u = 0; u < w; u++) {
            for (int v = 0; v < d; v++) {
                for (int y = -1; y > -8; y--) {
                    BlockState b = s.get(u, y, v);
                    if (b.isSolid() && !b.is(ModBlocks.ASH_LAYER.get()) && y < -1) break;
                    s.set(u, y, v, fill);
                }
            }
        }
    }

    /** Blast damage: bites out of the upper corners, rubble and ash heaped around the base. */
    static void ruin(Site s, int w, int d, int h, int bites) {
        for (int i = 0; i < bites; i++) {
            int cu = s.r.nextBoolean() ? s.r.nextInt(3) : w - 1 - s.r.nextInt(3);
            int cv = s.r.nextInt(d);
            int cy = h / 2 + s.r.nextInt(Math.max(1, h / 2 + 1));
            int rad = 2 + s.r.nextInt(2);
            for (int u = cu - rad; u <= cu + rad; u++) {
                for (int y = cy - rad; y <= cy + rad + 4; y++) {
                    for (int v = cv - rad; v <= cv + rad; v++) {
                        if (u < 0 || u >= w || v < 0 || v >= d || y < 1) continue;
                        int du = u - cu, dy = Math.min(0, y - cy), dv = v - cv;
                        if (du * du + dy * dy + dv * dv <= rad * rad + s.r.nextInt(3)) s.air(u, y, v);
                    }
                }
            }
        }
        for (int i = 0; i < w + d; i++) {
            int u = -2 + s.r.nextInt(w + 4), v = -2 + s.r.nextInt(d + 4);
            if (u >= 0 && u < w && v >= 0 && v < d) continue;
            BlockPos p = s.at(u, 0, v);
            BlockPos g = new BlockPos(p.getX(), WastelandFeatures.ground(s.level, p.getX(), p.getZ()), p.getZ());
            if (!WastelandFeatures.solidBelow(s.level, g) || !s.level.getBlockState(g).canBeReplaced()) continue;
            s.level.setBlock(g, s.r.nextInt(3) == 0 ? ModBlocks.RUBBLE.get().defaultBlockState() : WastelandFeatures.ash(1 + s.r.nextInt(3)), 2);
        }
    }

    // ------------------------------------------------------------------ apartment block
    /** A gutted concrete tower: storeys of window bays, slab balconies with rebar rails, rusted wall units. */
    public static boolean apartment(WorldGenLevel level, RandomSource r, BlockPos origin, Direction front) {
        Site s = new Site(level, r, origin, front);
        int w = 7 + r.nextInt(3) * 2, d = 6 + r.nextInt(3), floors = 3 + r.nextInt(4), fh = 4, h = floors * fh;
        int[] g = groundRange(s, w, d);
        if (g == null) return false;
        s = new Site(level, r, new BlockPos(origin.getX(), g[0], origin.getZ()), front);
        footing(s, w, d, concrete(r));
        boolean brickBase = r.nextBoolean();
        for (int y = 0; y <= h; y++) {
            int f = y / fh, ly = y % fh;
            for (int u = 0; u < w; u++) {
                for (int v = 0; v < d; v++) {
                    boolean edgeU = u == 0 || u == w - 1, edgeV = v == 0 || v == d - 1;
                    boolean shell = edgeU || edgeV;
                    if (ly == 0) {
                        // floor slab, with the odd hole punched through
                        if (y == 0 || shell || r.nextInt(9) != 0) s.set(u, y, v, concrete(r));
                        else s.air(u, y, v);
                        continue;
                    }
                    if (!shell) {
                        s.air(u, y, v);
                        continue;
                    }
                    boolean column = edgeU && edgeV;
                    boolean bay = edgeV ? (u % 3 != 0) : (v % 3 != 0);
                    BlockState wallMat = f == 0 && brickBase ? brick(r) : concrete(r);
                    if (column) {
                        s.set(u, y, v, concrete(r));
                    } else if (f == 0 && v == 0 && (u == w / 2) && ly <= 2) {
                        s.air(u, y, v); // street door
                    } else if (bay && (ly == 1 || ly == 2) && !(f == 0 && v == 0 && ly == 1 && u % 3 == 2 && r.nextBoolean())) {
                        s.set(u, y, v, window(r));
                    } else {
                        s.set(u, y, v, wallMat);
                    }
                }
            }
            // balconies on the street face, every other storey
            if (ly == 0 && f > 0 && f % 2 == 1) {
                int a = 1 + r.nextInt(Math.max(1, w / 2 - 1)), b = Math.min(w - 2, a + 2 + r.nextInt(3));
                for (int u = a; u <= b; u++) {
                    s.set(u, y, -1, concrete(r));
                    if (r.nextInt(5) != 0) s.set(u, y + 1, -1, ModBlocks.REBAR.get().defaultBlockState());
                }
            }
            // rusted wall units hung beside a window
            if (ly == 1 && f > 0 && r.nextInt(2) == 0) {
                int u = 1 + r.nextInt(w - 2);
                if (s.get(u, y, -1).isAir()) s.set(u, y, -1, ModBlocks.RUSTED_PLATING.get().defaultBlockState());
            }
        }
        // jagged roofline and a parapet where it survived
        for (int u = 0; u < w; u++) {
            for (int v = 0; v < d; v++) {
                if ((u == 0 || u == w - 1 || v == 0 || v == d - 1) && r.nextInt(3) != 0) s.set(u, h + 1, v, ModBlocks.SCORCHED_CONCRETE_WALL.get().defaultBlockState());
            }
        }
        if (r.nextBoolean()) { // rooftop water tank stub or aerial
            int u = 1 + r.nextInt(w - 2), v = 1 + r.nextInt(d - 2);
            for (int y = h + 1; y < h + 4 + r.nextInt(4); y++) s.set(u, y, v, ModBlocks.REBAR.get().defaultBlockState());
        }
        // a stair core of ladders up one inside wall
        int lu = w - 2;
        for (int y = 1; y < h; y++) {
            s.set(lu, y, d - 2, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, front));
            if (y % fh == 0) s.air(lu, y, d - 2);
        }
        for (int y = 1; y < h; y++) if (s.get(lu, y, d - 2).isAir()) s.set(lu, y, d - 2, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, front));
        s.chest(1 + r.nextInt(w - 3), 1 + fh * r.nextInt(floors), 1 + r.nextInt(d - 3));
        ruin(s, w, d, h, 1 + r.nextInt(3));
        s.finish();
        return true;
    }

    // ------------------------------------------------------------------ shopfront
    /** Two-storey brick shop: smashed display windows under a sagging charred awning, flat roof with a parapet. */
    public static boolean shop(WorldGenLevel level, RandomSource r, BlockPos origin, Direction front) {
        Site s = new Site(level, r, origin, front);
        int w = 7 + r.nextInt(4), d = 6 + r.nextInt(3), h = 8;
        int[] g = groundRange(s, w, d);
        if (g == null) return false;
        s = new Site(level, r, new BlockPos(origin.getX(), g[0], origin.getZ()), front);
        footing(s, w, d, brick(r));
        for (int y = 0; y <= h; y++) {
            for (int u = 0; u < w; u++) {
                for (int v = 0; v < d; v++) {
                    boolean shell = u == 0 || u == w - 1 || v == 0 || v == d - 1;
                    if (y == 0 || y == 4 || y == h) {
                        s.set(u, y, v, y == h && !shell && r.nextInt(6) == 0 ? Blocks.AIR.defaultBlockState()
                                : y == 0 ? concrete(r) : brick(r));
                    } else if (!shell) {
                        s.air(u, y, v);
                    } else if (v == 0 && y <= 3 && u > 0 && u < w - 1) {
                        // shop window and door
                        if (u == w / 2 && y <= 2) s.air(u, y, v);
                        else if (y == 3) s.set(u, y, v, brick(r));
                        else s.set(u, y, v, r.nextInt(3) == 0 ? ModBlocks.SHATTERED_GLASS.get().defaultBlockState() : Blocks.AIR.defaultBlockState());
                    } else if (y >= 5 && y <= 6 && (v == 0 || v == d - 1) && u % 2 == 1 && u < w - 1) {
                        s.set(u, y, v, window(r));
                    } else {
                        s.set(u, y, v, brick(r));
                    }
                }
            }
        }
        // awning of charred slabs, some of it fallen
        for (int u = 0; u < w; u++) {
            if (r.nextInt(4) == 0) continue;
            s.set(u, 4, -1, ModBlocks.CHARRED_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
            if (r.nextInt(3) == 0) s.set(u, 3, -1, ModBlocks.CHARRED_TRAPDOOR.get().defaultBlockState()
                    .setValue(TrapDoorBlock.FACING, front).setValue(TrapDoorBlock.OPEN, true));
        }
        for (int u = 0; u < w; u++) {
            for (int v = 0; v < d; v++) {
                if ((u == 0 || u == w - 1 || v == 0 || v == d - 1) && r.nextInt(4) != 0)
                    s.set(u, h + 1, v, ModBlocks.SCORCHED_BRICK_WALL.get().defaultBlockState());
            }
        }
        // counter, a crate and the stairs up are long gone: ladder instead
        for (int u = 1; u < w - 1; u++) if (r.nextInt(3) != 0) s.set(u, 1, d - 3, ModBlocks.CHARRED_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
        for (int y = 1; y < 5; y++) s.set(1, y, d - 2, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, front));
        s.air(1, 4, d - 2);
        s.set(1, 4, d - 2, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, front));
        s.chest(w - 2, 1, d - 2);
        if (r.nextInt(3) == 0) s.chest(w - 2, 5, d - 2);
        ruin(s, w, d, h, r.nextInt(3));
        s.finish();
        return true;
    }

    // ------------------------------------------------------------------ stilt shack
    /** A patchwork scrap hut perched on charred stilts, reached by a ladder, smoke stack on top. */
    public static boolean shack(WorldGenLevel level, RandomSource r, BlockPos origin, Direction front) {
        Site s = new Site(level, r, origin, front);
        int w = 5 + r.nextInt(3), d = 5 + r.nextInt(2), lift = 4 + r.nextInt(4), wallH = 3;
        int[] g = groundRange(s, w, d);
        if (g == null) return false;
        s = new Site(level, r, new BlockPos(origin.getX(), g[1], origin.getZ()), front);
        BlockState log = ModBlocks.CHARRED_LOG.get().defaultBlockState();
        int[][] legs = {{0, 0}, {w - 1, 0}, {0, d - 1}, {w - 1, d - 1}};
        for (int[] l : legs) {
            for (int y = lift - 1; y > -8; y--) {
                if (y < 0 && s.get(l[0], y, l[1]).isSolid() && !s.get(l[0], y, l[1]).is(ModBlocks.ASH_LAYER.get())) break;
                s.set(l[0], y, l[1], log);
            }
        }
        // cross bracing
        for (int u = 1; u < w - 1; u++) {
            s.set(u, lift - 2, 0, ModBlocks.CHARRED_FENCE.get().defaultBlockState());
            s.set(u, lift - 2, d - 1, ModBlocks.CHARRED_FENCE.get().defaultBlockState());
        }
        BlockState[] patch = {ModBlocks.RUSTED_PLATING.get().defaultBlockState(), ModBlocks.CHARRED_PLANKS.get().defaultBlockState(),
                ModBlocks.RUSTED_PLATING.get().defaultBlockState(), ModBlocks.SCRAP_PILE.get().defaultBlockState()};
        int y0 = lift;
        for (int u = -1; u <= w; u++) {
            for (int v = -1; v <= d; v++) {
                boolean inside = u >= 0 && u < w && v >= 0 && v < d;
                if (!inside && v != -1) continue;
                // floor, with a porch out front
                s.set(u, y0, v, ModBlocks.CHARRED_PLANKS.get().defaultBlockState());
                if (!inside) {
                    if (r.nextInt(3) != 0) s.set(u, y0 + 1, v, ModBlocks.CHARRED_FENCE.get().defaultBlockState());
                    continue;
                }
                boolean shell = u == 0 || u == w - 1 || v == 0 || v == d - 1;
                for (int y = 1; y <= wallH; y++) {
                    if (!shell) {
                        s.air(u, y0 + y, v);
                        continue;
                    }
                    BlockState m = patch[(u * 3 + v * 5 + y * 7 + r.nextInt(2)) % patch.length];
                    if (m.is(ModBlocks.SCRAP_PILE.get())) m = ModBlocks.RUSTED_PLATING.get().defaultBlockState();
                    boolean door = v == 0 && u == w / 2 && y <= 2;
                    boolean win = y == 2 && ((v == 0 && (u == 1 || u == w - 2)) || (u == 0 || u == w - 1) && v == d / 2);
                    s.set(u, y0 + y, v, door ? Blocks.AIR.defaultBlockState() : win ? window(r) : m);
                }
            }
        }
        // pitched roof of charred stairs, ridge along u
        int half = (d + 1) / 2;
        for (int u = -1; u <= w; u++) {
            for (int i = 0; i < half; i++) {
                int y = y0 + wallH + 1 + i;
                if (r.nextInt(8) == 0) continue; // holes in the roof
                s.set(u, y, i - 1 + 0, stair(front.getOpposite()));
                s.set(u, y, d - i, stair(front));
            }
            if (d % 2 == 1) s.set(u, y0 + wallH + half, half - 1, ModBlocks.CHARRED_SLAB.get().defaultBlockState());
        }
        // smoke stack
        int su = r.nextBoolean() ? 1 : w - 2;
        for (int y = y0 + 1; y < y0 + wallH + half + 3; y++) s.set(su, y, d - 2, brick(r));
        // ladder down a front leg
        for (int y = y0; y > -8; y--) {
            if (y < y0 && s.get(0, y, -1).isSolid()) break;
            if (y == y0) continue;
            s.set(0, y, -1, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, front));
        }
        s.air(0, y0, -1);
        s.set(0, y0, -1, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, front));
        s.chest(w - 2, y0 + 1, d - 2);
        s.finish();
        return true;
    }

    static BlockState stair(Direction facing) {
        return ModBlocks.CHARRED_STAIRS.get().defaultBlockState().setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, Half.BOTTOM);
    }

    /** Lets /place feature drop a single building (facing north) for testing and builders. */
    public static class Placer extends net.minecraft.world.level.levelgen.feature.Feature<net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration> {
        private final int kind;

        public Placer(int kind) {
            super(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC);
            this.kind = kind;
        }

        @Override
        public boolean place(net.minecraft.world.level.levelgen.feature.FeaturePlaceContext<net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration> ctx) {
            return switch (kind) {
                case 0 -> apartment(ctx.level(), ctx.random(), ctx.origin(), Direction.NORTH);
                case 1 -> shop(ctx.level(), ctx.random(), ctx.origin(), Direction.NORTH);
                default -> shack(ctx.level(), ctx.random(), ctx.origin(), Direction.NORTH);
            };
        }
    }
}
