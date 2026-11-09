package com.lach.affermath.world;

import com.lach.affermath.entity.Cratomancer;
import com.lach.affermath.registry.ModBlocks;
import com.lach.affermath.registry.ModEntities;
import com.lach.affermath.registry.ModWorld;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * Pre-war bunker: a concrete pillbox over a ladder shaft, corridors with barracks and stores,
 * a blast door, then stairs down to the vault where the Cratomancer waits.
 */
public class BunkerStructure extends Structure {
    public static final MapCodec<BunkerStructure> CODEC = simpleCodec(BunkerStructure::new);

    public BunkerStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public StructureType<?> type() {
        return ModWorld.BUNKER.get();
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext ctx) {
        int x = ctx.chunkPos().getMiddleBlockX(), z = ctx.chunkPos().getMiddleBlockZ();
        int y = ctx.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, ctx.heightAccessor(), ctx.randomState());
        if (y <= ctx.chunkGenerator().getSeaLevel()) return Optional.empty();
        if (y - 40 < ctx.heightAccessor().getMinY() + 6) return Optional.empty();
        BlockPos pos = new BlockPos(x, y, z);
        long seed = ctx.random().nextLong();
        Rotation rot = Rotation.getRandom(ctx.random());
        return Optional.of(new GenerationStub(pos, b -> b.addPiece(new Piece(pos, rot, seed))));
    }

    public static class Piece extends PlannedPiece {
        public Piece(BlockPos origin, Rotation rot, long seed) {
            super(ModWorld.BUNKER_PIECE.get(), origin, rot, seed, 0);
        }

        public Piece(CompoundTag tag) {
            super(ModWorld.BUNKER_PIECE.get(), tag);
        }

        private static BlockState wall(RandomSource r) {
            float f = r.nextFloat();
            if (f < 0.25f) return ModBlocks.CRACKED_SCORCHED_CONCRETE.get().defaultBlockState();
            if (f < 0.3f) return Blocks.POLISHED_ANDESITE.defaultBlockState();
            return ModBlocks.SCORCHED_CONCRETE.get().defaultBlockState();
        }

        private static final BlockState AIR = Blocks.AIR.defaultBlockState();

        /** Hollow room: walls/floor/ceiling of concrete, air inside. Coordinates are inclusive interior. */
        private static void room(Plan p, RandomSource r, int x0, int y0, int z0, int x1, int y1, int z1) {
            for (int x = x0 - 1; x <= x1 + 1; x++)
                for (int y = y0 - 1; y <= y1 + 1; y++)
                    for (int z = z0 - 1; z <= z1 + 1; z++) {
                        boolean inside = x >= x0 && x <= x1 && y >= y0 && y <= y1 && z >= z0 && z <= z1;
                        if (inside) p.set(x, y, z, AIR);
                        else {
                            BlockState cur = p.get(x, y, z);
                            if (cur == null || !cur.isAir()) p.set(x, y, z, wall(r));
                        }
                    }
        }

        private static void carve(Plan p, int x0, int y0, int z0, int x1, int y1, int z1) {
            p.fill(x0, y0, z0, x1, y1, z1, AIR);
        }

        private static BlockState lantern() {
            return Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
        }

        @Override
        protected void build(Plan p, RandomSource r) {
            int L = -24;       // corridor floor level (relative), air from L+1..L+3
            int V = L - 7;     // vault floor
            // --- surface pillbox, 7x7 outer, door on -z side
            for (int x = -3; x <= 3; x++)
                for (int z = -3; z <= 3; z++) {
                    p.foundation(x, z);
                    p.set(x, 0, z, ModBlocks.SCORCHED_CONCRETE.get().defaultBlockState());
                    for (int y = 1; y <= 4; y++) {
                        boolean edge = Math.abs(x) == 3 || Math.abs(z) == 3;
                        p.set(x, y, z, edge || y == 4 ? wall(r) : AIR);
                    }
                    for (int y = 5; y <= 7; y++) p.set(x, y, z, AIR);
                }
            p.set(0, 1, -3, AIR);
            p.set(0, 2, -3, AIR);
            // gun slits
            for (int s = -1; s <= 1; s++) {
                p.set(s, 3, 3, Blocks.IRON_BARS.defaultBlockState());
                p.set(3, 3, s, Blocks.IRON_BARS.defaultBlockState());
                p.set(-3, 3, s, Blocks.IRON_BARS.defaultBlockState());
            }
            // sandbag ring
            for (int x = -5; x <= 5; x++)
                for (int z = -5; z <= 5; z++) {
                    if ((Math.abs(x) == 5 || Math.abs(z) == 5) && !(x == 0 || x == 1 || x == -1) && r.nextFloat() < 0.75f)
                        p.set(x, 1, z, ModBlocks.SANDBAGS.get().defaultBlockState());
                }
            // broken roof
            for (int i = 0; i < 4; i++) p.set(-2 + r.nextInt(5), 4, -2 + r.nextInt(5), AIR);
            // --- shaft with ladder from pillbox floor down to corridor
            for (int y = L + 1; y <= 0; y++) {
                for (int x = -1; x <= 1; x++)
                    for (int z = -1; z <= 1; z++) {
                        boolean inner = x == 0 && z == 0;
                        if (y == 0 && inner) p.set(x, y, z, AIR);
                        else if (y < 0) p.set(x, y, z, inner ? AIR : wall(r));
                    }
                p.set(0, y, 0, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
            }
            p.set(0, 0, -1, ModBlocks.SCORCHED_CONCRETE.get().defaultBlockState());
            p.set(0, 1, -1, Blocks.IRON_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.OPEN, true)
                    .setValue(TrapDoorBlock.FACING, Direction.SOUTH).setValue(TrapDoorBlock.HALF, net.minecraft.world.level.block.state.properties.Half.TOP));
            p.set(0, 1, -1, AIR);
            // --- landing room at the bottom of the shaft
            room(p, r, -2, L + 1, -2, 2, L + 3, 2);
            for (int y = L + 1; y <= L + 3; y++) p.set(0, y, 0, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
            p.set(0, L + 1, -1, ModBlocks.SCORCHED_CONCRETE.get().defaultBlockState());
            p.set(0, L + 2, -1, ModBlocks.SCORCHED_CONCRETE.get().defaultBlockState());
            p.set(0, L + 3, -1, ModBlocks.SCORCHED_CONCRETE.get().defaultBlockState());
            p.set(0, L + 3, 2, lantern());
            // --- corridor east to the hub
            room(p, r, 3, L + 1, -1, 14, L + 3, 1);
            carve(p, 2, L + 1, -1, 3, L + 3, 1);
            p.set(8, L + 3, 0, lantern());
            // --- hub
            room(p, r, 15, L + 1, -4, 23, L + 4, 4);
            p.set(19, L + 4, 0, lantern());
            p.spawners.add(p.at(19, L + 1, 0));
            p.blocks.remove(p.at(19, L + 1, 0));
            // barracks (north, -z)
            room(p, r, 15, L + 1, -12, 23, L + 3, -6);
            carve(p, 18, L + 1, -5, 20, L + 3, -5);
            for (int x = 16; x <= 22; x += 2) {
                p.set(x, L + 1, -11, Blocks.GRAY_WOOL.defaultBlockState());
                p.set(x, L + 1, -10, Blocks.GRAY_WOOL.defaultBlockState());
            }
            p.set(19, L + 3, -9, lantern());
            p.chest(22, L + 1, -7, Loot.BUNKER);
            // stores (south, +z)
            room(p, r, 15, L + 1, 6, 23, L + 3, 12);
            carve(p, 18, L + 1, 5, 20, L + 3, 5);
            for (int x = 15; x <= 23; x++) {
                if (r.nextFloat() < 0.5f) p.set(x, L + 1, 12, Blocks.BARREL.defaultBlockState());
                if (r.nextFloat() < 0.3f) p.set(x, L + 2, 12, Blocks.BARREL.defaultBlockState());
            }
            p.chest(15, L + 1, 7, Loot.BUNKER);
            p.chest(23, L + 1, 9, Loot.BUNKER);
            p.set(19, L + 3, 9, lantern());
            // --- corridor to the blast door
            room(p, r, 24, L + 1, -1, 33, L + 3, 1);
            carve(p, 23, L + 1, -1, 24, L + 3, 1);
            // blast door frame: iron blocks with a torn-open gap
            for (int y = L; y <= L + 4; y++)
                for (int z = -2; z <= 2; z++) p.set(34, y, z, Blocks.IRON_BLOCK.defaultBlockState());
            carve(p, 34, L + 1, -1, 34, L + 3, 1);
            p.set(34, L + 3, -1, Blocks.IRON_BLOCK.defaultBlockState());
            p.set(34, L + 1, 1, Blocks.IRON_BARS.defaultBlockState());
            // --- stairs down to the vault
            int y = L;
            for (int x = 35; x <= 41; x++) {
                y--;
                for (int z = -2; z <= 2; z++)
                    for (int yy = y - 1; yy <= y + 5; yy++) {
                        boolean inner = Math.abs(z) <= 1 && yy >= y + 1 && yy <= y + 4;
                        if (inner) p.set(x, yy, z, AIR);
                        else if (yy == y && Math.abs(z) <= 1)
                            p.set(x, yy, z, Blocks.POLISHED_ANDESITE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST));
                        else p.set(x, yy, z, wall(r));
                    }
            }
            // --- vault 17x17 interior, floor at V
            int vx0 = 42, vz0 = -8;
            room(p, r, vx0, V + 1, vz0, vx0 + 16, V + 9, vz0 + 16);
            carve(p, 41, y + 1, -1, 42, y + 4, 1);
            for (int x = vx0; x <= vx0 + 16; x++)
                for (int z = vz0; z <= vz0 + 16; z++)
                    p.set(x, V, z, (x + z) % 3 == 0 ? ModBlocks.CRACKED_SCORCHED_CONCRETE.get().defaultBlockState()
                            : Blocks.POLISHED_DEEPSLATE.defaultBlockState());
            // pillars
            int[][] pil = {{vx0 + 3, vz0 + 3}, {vx0 + 12, vz0 + 3}, {vx0 + 3, vz0 + 12}, {vx0 + 12, vz0 + 12}};
            for (int[] c : pil) {
                for (int yy = V + 1; yy <= V + 9; yy++)
                    for (int a = 0; a <= 1; a++)
                        for (int b = 0; b <= 1; b++) p.set(c[0] + a, yy, c[1] + b, wall(r));
                p.set(c[0], V + 6, c[1] - 1, Blocks.SOUL_LANTERN.defaultBlockState());
                p.set(c[0] + 1, V + 6, c[1] + 2, Blocks.SOUL_LANTERN.defaultBlockState());
            }
            // rubble, ash and a crater in the vault floor
            for (int i = 0; i < 30; i++) {
                int x = vx0 + r.nextInt(17), z = vz0 + r.nextInt(17);
                BlockState cur = p.get(x, V + 1, z);
                if (cur != null && cur.isAir())
                    p.set(x, V + 1, z, r.nextBoolean() ? ModBlocks.RUBBLE.get().defaultBlockState()
                            : ModBlocks.ASH_LAYER.get().defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1 + r.nextInt(3)));
            }
            // war table + vault chests at the far wall
            p.set(vx0 + 8, V + 1, vz0 + 15, Blocks.LECTERN.defaultBlockState());
            p.chest(vx0 + 6, V + 1, vz0 + 16, Loot.VAULT);
            p.chest(vx0 + 10, V + 1, vz0 + 16, Loot.VAULT);
            p.boss = p.at(vx0 + 8, V + 1, vz0 + 8);
        }

        @Override
        protected void onWrite(WorldGenLevel level, Plan p, BoundingBox chunkBox) {
            if (p.boss == null || !chunkBox.isInside(p.boss)) return;
            Cratomancer boss = ModEntities.CRATOMANCER.get().create(level.getLevel(), EntitySpawnReason.STRUCTURE);
            if (boss == null) return;
            boss.snapTo(p.boss.getX() + 0.5, p.boss.getY(), p.boss.getZ() + 0.5, 0, 0);
            boss.finalizeSpawn(level, level.getCurrentDifficultyAt(p.boss), EntitySpawnReason.STRUCTURE, null);
            boss.setPersistenceRequired();
            boss.setHome(p.boss);
            level.addFreshEntityWithPassengers(boss);
        }
    }
}
