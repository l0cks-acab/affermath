package com.lach.affermath.world;

import com.lach.affermath.registry.ModBlocks;
import com.lach.affermath.registry.ModWorld;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Bombed-out buildings: houses, a shelled church with a tower, trench lines, burnt vehicle husks. */
public class RuinStructure extends Structure {
    public static final MapCodec<RuinStructure> CODEC = simpleCodec(RuinStructure::new);

    public RuinStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public StructureType<?> type() {
        return ModWorld.RUIN.get();
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext ctx) {
        int x = ctx.chunkPos().getMiddleBlockX(), z = ctx.chunkPos().getMiddleBlockZ();
        int y = ctx.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, ctx.heightAccessor(), ctx.randomState());
        if (y <= ctx.chunkGenerator().getSeaLevel()) return Optional.empty();
        BlockPos pos = new BlockPos(x, y, z);
        long seed = ctx.random().nextLong();
        int roll = ctx.random().nextInt(100);
        int variant = roll < 45 ? 0 : roll < 63 ? 1 : roll < 85 ? 2 : 3;
        Rotation rot = Rotation.getRandom(ctx.random());
        return Optional.of(new GenerationStub(pos, b -> b.addPiece(new Piece(pos, rot, seed, variant))));
    }

    public static class Piece extends PlannedPiece {
        public Piece(BlockPos origin, Rotation rot, long seed, int variant) {
            super(ModWorld.RUIN_PIECE.get(), origin, rot, seed, variant);
        }

        public Piece(CompoundTag tag) {
            super(ModWorld.RUIN_PIECE.get(), tag);
        }

        @Override
        protected void build(Plan p, RandomSource r) {
            switch (variant) {
                case 1 -> church(p, r);
                case 2 -> trench(p, r);
                case 3 -> vehicle(p, r);
                default -> house(p, r);
            }
        }

        private static BlockState brick(RandomSource r) {
            float f = r.nextFloat();
            if (f < 0.3f) return ModBlocks.CRACKED_SCORCHED_BRICKS.get().defaultBlockState();
            return ModBlocks.SCORCHED_BRICKS.get().defaultBlockState();
        }

        private static BlockState air() {
            return Blocks.AIR.defaultBlockState();
        }

        /** Remove a fraction of the wall blocks, more the higher up they are, and punch a few blast holes. */
        private static void damage(Plan p, RandomSource r, int height, int blasts, int minX, int maxX, int minZ, int maxZ) {
            List<BlockPos> rubble = new ArrayList<>();
            for (var e : new ArrayList<>(p.blocks.entrySet())) {
                if (e.getValue().isAir()) continue;
                int dy = e.getKey().getY() - p.origin.getY();
                if (dy <= 0) continue;
                float chance = 0.06f + 0.5f * dy / (float) height;
                if (r.nextFloat() < chance * 0.6f) {
                    p.blocks.put(e.getKey(), air());
                    if (r.nextFloat() < 0.3f) rubble.add(e.getKey());
                }
            }
            for (int b = 0; b < blasts; b++) {
                int bx = minX + r.nextInt(Math.max(1, maxX - minX + 1));
                int bz = minZ + r.nextInt(Math.max(1, maxZ - minZ + 1));
                int by = 1 + r.nextInt(Math.max(1, height));
                int rad = 2 + r.nextInt(3);
                for (int x = -rad; x <= rad; x++)
                    for (int y = -rad; y <= rad; y++)
                        for (int z = -rad; z <= rad; z++) {
                            if (x * x + y * y + z * z > rad * rad + r.nextInt(3)) continue;
                            if (by + y <= 0) continue;
                            BlockPos q = p.at(bx + x, by + y, bz + z);
                            BlockState s = p.blocks.get(q);
                            if (s != null && !s.isAir()) {
                                p.blocks.put(q, air());
                                rubble.add(q);
                            }
                        }
            }
            // debris settles on the floor
            for (BlockPos q : rubble) {
                BlockPos floor = new BlockPos(q.getX(), p.origin.getY() + 1, q.getZ());
                BlockState s = p.blocks.get(floor);
                if (s == null || s.isAir()) {
                    p.blocks.put(floor, r.nextBoolean() ? ModBlocks.RUBBLE.get().defaultBlockState()
                            : ModBlocks.ASH_LAYER.get().defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1 + r.nextInt(4)));
                }
            }
        }

        private void house(Plan p, RandomSource r) {
            int w = 6 + r.nextInt(5), d = 6 + r.nextInt(5), h = 4 + r.nextInt(3);
            for (int x = 0; x <= w; x++)
                for (int z = 0; z <= d; z++) {
                    p.foundation(x, z);
                    boolean edge = x == 0 || z == 0 || x == w || z == d;
                    p.set(x, 0, z, r.nextFloat() < 0.7f ? ModBlocks.CHARRED_PLANKS.get().defaultBlockState() : ModBlocks.RUBBLE.get().defaultBlockState());
                    for (int y = 1; y <= h; y++) {
                        if (edge) {
                            boolean corner = (x == 0 || x == w) && (z == 0 || z == d);
                            p.set(x, y, z, corner ? ModBlocks.CHARRED_LOG.get().defaultBlockState() : brick(r));
                        } else p.set(x, y, z, air());
                    }
                    for (int y = h + 1; y <= h + 3; y++) p.set(x, y, z, air());
                }
            // door + windows
            int door = 1 + r.nextInt(w - 1);
            p.set(door, 1, 0, air());
            p.set(door, 2, 0, air());
            for (int x = 2; x < w - 1; x += 3) {
                p.set(x, 2, d, ModBlocks.SHATTERED_GLASS.get().defaultBlockState());
                p.set(x, 3, d, air());
            }
            for (int z = 2; z < d - 1; z += 3) {
                p.set(0, 2, z, ModBlocks.SHATTERED_GLASS.get().defaultBlockState());
                p.set(w, 2, z, air());
            }
            // half-collapsed upper floor
            for (int x = 1; x < w; x++)
                for (int z = 1; z < d; z++)
                    if (x < w / 2 + r.nextInt(2) && r.nextFloat() < 0.75f) p.set(x, h - 1, z, ModBlocks.CHARRED_PLANKS.get().defaultBlockState());
            damage(p, r, h, 1 + r.nextInt(2), 0, w, 0, d);
            if (r.nextFloat() < 0.6f) p.chest(1 + r.nextInt(w - 1), 1, 1 + r.nextInt(d - 1), Loot.RUIN);
        }

        private void church(Plan p, RandomSource r) {
            int w = 10, d = 18, h = 8, tw = 5, th = 15 + r.nextInt(6);
            for (int x = 0; x <= w; x++)
                for (int z = 0; z <= d; z++) {
                    p.foundation(x, z);
                    boolean edge = x == 0 || z == 0 || x == w || z == d;
                    p.set(x, 0, z, (x + z) % 2 == 0 ? Blocks.POLISHED_ANDESITE.defaultBlockState() : ModBlocks.CRACKED_SCORCHED_CONCRETE.get().defaultBlockState());
                    for (int y = 1; y <= h; y++) {
                        if (edge) p.set(x, y, z, brick(r));
                        else p.set(x, y, z, air());
                    }
                    for (int y = h + 1; y <= h + 4; y++) p.set(x, y, z, air());
                }
            // gable remains on the back wall
            for (int y = h + 1; y <= h + 4; y++)
                for (int x = y - h; x <= w - (y - h); x++) p.set(x, y, d, brick(r));
            // tall arched window gaps
            for (int z = 3; z < d - 1; z += 4) {
                for (int y = 3; y <= 6; y++) {
                    p.set(0, y, z, air());
                    p.set(w, y, z, air());
                }
            }
            // tower at the front, door through it
            int tx = (w - tw) / 2;
            for (int x = tx; x <= tx + tw - 1; x++)
                for (int z = -tw + 1; z <= 0; z++) {
                    p.foundation(x, z);
                    boolean edge = x == tx || x == tx + tw - 1 || z == -tw + 1 || z == 0;
                    p.set(x, 0, z, ModBlocks.SCORCHED_CONCRETE.get().defaultBlockState());
                    for (int y = 1; y <= th; y++) {
                        if (edge) p.set(x, y, z, brick(r));
                        else p.set(x, y, z, air());
                    }
                }
            for (int y = 1; y <= 3; y++)
                for (int z = -tw + 1; z <= 0; z++) p.set(tx + tw / 2, y, z, air());
            // belfry openings
            for (int y = th - 4; y <= th - 2; y++) {
                p.set(tx + tw / 2, y, -tw + 1, air());
                p.set(tx + tw / 2, y, 0, air());
                p.set(tx, y, -tw / 2, air());
                p.set(tx + tw - 1, y, -tw / 2, air());
            }
            // jagged broken crown
            for (int x = tx; x <= tx + tw - 1; x++)
                for (int z = -tw + 1; z <= 0; z++) {
                    int cut = r.nextInt(4);
                    for (int y = th - cut + 1; y <= th; y++) p.set(x, y, z, air());
                }
            // pews of charred wood
            for (int z = 3; z < d - 3; z += 2) {
                for (int x = 2; x <= 3; x++) if (r.nextFloat() < 0.7f) p.set(x, 1, z, ModBlocks.CHARRED_PLANKS.get().defaultBlockState());
                for (int x = w - 3; x <= w - 2; x++) if (r.nextFloat() < 0.7f) p.set(x, 1, z, ModBlocks.CHARRED_PLANKS.get().defaultBlockState());
            }
            damage(p, r, h, 3 + r.nextInt(2), 0, w, 1, d);
            p.chest(w / 2, 1, d - 1, Loot.RUIN);
            if (r.nextBoolean()) p.chest(tx + 1, 1, -tw + 2, Loot.RUIN);
        }

        private void trench(Plan p, RandomSource r) {
            int len = 14 + r.nextInt(10);
            int zig = 0;
            BlockState bag = ModBlocks.SANDBAGS.get().defaultBlockState();
            for (int x = 0; x < len; x++) {
                if (x % 6 == 5) zig = zig == 0 ? 2 : 0;
                for (int z = zig; z <= zig + 2; z++) {
                    p.set(x, 0, z, ModBlocks.RUBBLE.get().defaultBlockState());
                    p.set(x, 0, z, Blocks.MUD.defaultBlockState());
                    p.set(x, 1, z, air());
                    p.set(x, 2, z, air());
                }
                p.set(x, -1, zig + 1, Blocks.SPRUCE_PLANKS.defaultBlockState());
                p.set(x, 0, zig + 1, ModBlocks.CHARRED_PLANKS.get().defaultBlockState());
                if (r.nextFloat() < 0.85f) p.set(x, 3, zig - 1, bag);
                if (r.nextFloat() < 0.85f) p.set(x, 3, zig + 3, bag);
                if (r.nextFloat() < 0.4f) p.set(x, 4, zig - 1, bag);
                for (int y = 0; y <= 2; y++) {
                    if (p.get(x, y, zig - 1) == null) p.set(x, y, zig - 1, Blocks.PACKED_MUD.defaultBlockState());
                    if (p.get(x, y, zig + 3) == null) p.set(x, y, zig + 3, Blocks.PACKED_MUD.defaultBlockState());
                }
                if (x % 4 == 0 && r.nextBoolean()) p.set(x, 3, zig + 4, ModBlocks.REBAR.get().defaultBlockState());
            }
            // lower the trench into the ground
            Plan shifted = new Plan(p.origin.below(2), p.rot);
            for (var e : p.blocks.entrySet()) shifted.blocks.put(e.getKey().below(2), e.getValue());
            p.blocks.clear();
            p.blocks.putAll(shifted.blocks);
            if (r.nextFloat() < 0.7f) p.chest(len / 2, -1, zig + 1, Loot.RUIN);
        }

        private void vehicle(Plan p, RandomSource r) {
            BlockState hull = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
            BlockState dark = Blocks.COAL_BLOCK.defaultBlockState();
            BlockState wheel = ModBlocks.CHARRED_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, net.minecraft.core.Direction.Axis.Z);
            for (int x = 0; x <= 7; x++) {
                p.set(x, 1, 0, wheel);
                p.set(x, 1, 4, wheel);
                for (int z = 1; z <= 3; z++) p.set(x, 1, z, dark);
                for (int z = 0; z <= 4; z++) p.set(x, 2, z, r.nextFloat() < 0.2f ? dark : hull);
            }
            for (int x = 2; x <= 5; x++)
                for (int z = 1; z <= 3; z++) p.set(x, 3, z, r.nextFloat() < 0.25f ? air() : hull);
            // barrel
            for (int x = 6; x <= 10; x++) p.set(x, 3, 2, Blocks.IRON_BARS.defaultBlockState());
            p.set(3, 4, 2, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
            for (int x = -2; x <= 9; x++)
                for (int z = -2; z <= 6; z++)
                    if (r.nextFloat() < 0.25f && p.get(x, 1, z) == null)
                        p.set(x, 1, z, ModBlocks.ASH_LAYER.get().defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1 + r.nextInt(3)));
            if (r.nextFloat() < 0.5f) p.chest(4, 3, 2, Loot.RUIN);
        }
    }
}
