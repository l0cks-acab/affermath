package com.lach.affermath.world;

import com.lach.affermath.compat.GB;
import com.lach.affermath.registry.ModBlocks;
import com.lach.affermath.registry.ModWorld;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * Derelict nuclear power station: two great hyperboloid cooling towers (one broken open),
 * a breached reactor dome flooded with sludge, a turbine hall, a brick flue stack, overhead
 * pipe runs, a control block with loot and a soldier spawner, all on a cracked concrete apron
 * behind a rebar fence. Rare; terrain is boxed flat around it.
 */
public class PowerPlantStructure extends Structure {
    public static final MapCodec<PowerPlantStructure> CODEC = simpleCodec(PowerPlantStructure::new);

    public PowerPlantStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public StructureType<?> type() {
        return ModWorld.POWER_PLANT.get();
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext ctx) {
        int x = ctx.chunkPos().getMiddleBlockX(), z = ctx.chunkPos().getMiddleBlockZ();
        int lo = Integer.MAX_VALUE, hi = Integer.MIN_VALUE;
        for (int dx = -32; dx <= 32; dx += 32) {
            for (int dz = -24; dz <= 24; dz += 24) {
                int y = ctx.chunkGenerator().getFirstOccupiedHeight(x + dx, z + dz, Heightmap.Types.WORLD_SURFACE_WG, ctx.heightAccessor(), ctx.randomState());
                lo = Math.min(lo, y);
                hi = Math.max(hi, y);
            }
        }
        if (hi - lo > 14 || lo <= ctx.chunkGenerator().getSeaLevel()) return Optional.empty();
        BlockPos pos = new BlockPos(x, (lo + hi) / 2, z);
        long seed = ctx.random().nextLong();
        Rotation rot = Rotation.getRandom(ctx.random());
        return Optional.of(new GenerationStub(pos, b -> b.addPiece(new Piece(pos, rot, seed))));
    }

    public static class Piece extends PlannedPiece {
        static final int PX = 42, PZ = 32; // apron half-size

        public Piece(BlockPos origin, Rotation rot, long seed) {
            super(ModWorld.POWER_PLANT_PIECE.get(), origin, rot, seed, 0);
        }

        public Piece(CompoundTag tag) {
            super(ModWorld.POWER_PLANT_PIECE.get(), tag);
        }

        private static final BlockState AIR = Blocks.AIR.defaultBlockState();

        static BlockState concrete(RandomSource r) {
            float f = r.nextFloat();
            if (f < 0.28f) return ModBlocks.CRACKED_SCORCHED_CONCRETE.get().defaultBlockState();
            return ModBlocks.SCORCHED_CONCRETE.get().defaultBlockState();
        }

        static BlockState brick(RandomSource r) {
            return (r.nextInt(3) == 0 ? ModBlocks.CRACKED_SCORCHED_BRICKS : ModBlocks.SCORCHED_BRICKS).get().defaultBlockState();
        }

        static BlockState plate() {
            return ModBlocks.RUSTED_PLATING.get().defaultBlockState();
        }

        static BlockState rebar() {
            return ModBlocks.REBAR.get().defaultBlockState();
        }

        @Override
        protected void build(Plan p, RandomSource r) {
            apron(p, r);
            coolingTower(p, r, -24, -13, false);
            coolingTower(p, r, -24, 17, true);
            reactor(p, r, 10, 0);
            turbineHall(p, r);
            controlBlock(p, r);
            flueStack(p, r, 34, 2);
            pipes(p, r);
            fence(p, r);
            debris(p, r);
        }

        // ------------------------------------------------------------------ apron
        private void apron(Plan p, RandomSource r) {
            for (int x = -PX; x <= PX; x++) {
                for (int z = -PZ; z <= PZ; z++) {
                    for (int y = -4; y <= -2; y++) p.set(x, y, z, concrete(r));
                    // the surface: slabs of concrete heaved and cracked, ash ground showing through
                    int k = r.nextInt(20);
                    BlockState top = k == 0 ? ModBlocks.ASHEN_SOIL.get().defaultBlockState()
                            : k == 1 ? ModBlocks.RUBBLE.get().defaultBlockState() : concrete(r);
                    p.set(x, -1, z, top);
                    // painted bay lines on the apron
                    if ((x % 12 == 0 && Math.abs(z) > 26) && r.nextInt(4) != 0) p.set(x, -1, z, ModBlocks.ASPHALT_LINE.get().defaultBlockState());
                }
            }
        }

        // ------------------------------------------------------------------ cooling tower
        /** Hyperboloid shell: wide base on raking legs, pinched waist, flared lip. */
        private void coolingTower(Plan p, RandomSource r, int cx, int cz, boolean broken) {
            int H = 46;
            double base = 15, waist = 10, yw = H * 0.7;
            double c = yw / Math.sqrt((base / waist) * (base / waist) - 1);
            // collapse: a wedge of the upper shell is gone, the edge ragged
            double breakAngle = r.nextDouble() * Math.PI * 2, breakWidth = 1.2 + r.nextDouble() * 0.8;
            int breakFrom = (int) (H * (0.4 + r.nextDouble() * 0.2));
            for (int y = 0; y <= H; y++) {
                double rad = waist * Math.sqrt(1 + ((y - yw) / c) * ((y - yw) / c));
                int R = (int) Math.ceil(rad) + 1;
                for (int x = -R; x <= R; x++) {
                    for (int z = -R; z <= R; z++) {
                        double d = Math.sqrt(x * x + z * z);
                        if (Math.abs(d - rad) > 0.75) continue;
                        double ang = Math.atan2(z, x);
                        if (y < 4) {
                            // raking support legs with open arches between them
                            int leg = (int) Math.floor((ang + Math.PI) / (Math.PI * 2) * 36);
                            if (leg % 3 != 0) continue;
                        }
                        if (broken) {
                            double da = Math.abs(Math.atan2(Math.sin(ang - breakAngle), Math.cos(ang - breakAngle)));
                            int edge = breakFrom + (int) ((da / breakWidth) * 18) + (int) (Math.sin(ang * 7) * 2) + r.nextInt(2);
                            if (da < breakWidth && y > edge) continue;
                        }
                        BlockState s = concrete(r);
                        if (y == H || y == H - 1) s = ModBlocks.SCORCHED_CONCRETE.get().defaultBlockState(); // lip
                        else if (y % 9 == 4 && r.nextInt(3) != 0) s = ModBlocks.CRACKED_SCORCHED_CONCRETE.get().defaultBlockState(); // pour lines
                        else if (y > H - 8 && r.nextInt(3) == 0) s = ModBlocks.CHARRED_PLANKS.get().defaultBlockState(); // soot streaking under the lip
                        p.set(cx + x, y, cz + z, s);
                    }
                }
            }
            // basin floor inside, sludge pooled in the old cooling pond
            for (int x = -13; x <= 13; x++) {
                for (int z = -13; z <= 13; z++) {
                    double d = Math.sqrt(x * x + z * z);
                    if (d > 13.5) continue;
                    p.set(cx + x, -1, cz + z, d < 10 && r.nextInt(5) != 0 ? GB.block("irradiated_soil") : concrete(r));
                    if (d < 9 && r.nextInt(3) != 0) p.set(cx + x, 0, cz + z, ModBlocks.TOXIC_SLUDGE.get().defaultBlockState());
                }
            }
            // a service ladder up the outside
            double la = r.nextDouble() * Math.PI * 2;
            if (broken) la = breakAngle + Math.PI;
            for (int y = 4; y < H; y++) {
                double rad = waist * Math.sqrt(1 + ((y - yw) / c) * ((y - yw) / c));
                int x = (int) Math.round(Math.cos(la) * (rad + 1)), z = (int) Math.round(Math.sin(la) * (rad + 1));
                if (p.get(cx + x, y, cz + z) == null) p.set(cx + x, y, cz + z, rebar());
            }
            if (broken) {
                // the fallen section, heaped against the base on the broken side
                for (int i = 0; i < 260; i++) {
                    double a = breakAngle + (r.nextDouble() - 0.5) * breakWidth * 1.6;
                    double dist = 15 + r.nextDouble() * 9;
                    int x = (int) Math.round(Math.cos(a) * dist), z = (int) Math.round(Math.sin(a) * dist);
                    int h = (int) Math.max(0, 4 - Math.abs(dist - 18) * 0.7 + r.nextInt(2));
                    for (int y = 0; y < h; y++) {
                        int ax = cx + x, az = cz + z;
                        if (Math.abs(ax) > PX || Math.abs(az) > PZ) continue;
                        p.set(ax, y, az, r.nextInt(3) == 0 ? ModBlocks.RUBBLE.get().defaultBlockState() : concrete(r));
                    }
                }
            }
        }

        // ------------------------------------------------------------------ reactor
        private void reactor(Plan p, RandomSource r, int cx, int cz) {
            int hw = 10, h = 12;
            for (int x = -hw; x <= hw; x++) {
                for (int z = -hw; z <= hw; z++) {
                    boolean wall = Math.abs(x) == hw || Math.abs(z) == hw;
                    for (int y = 0; y <= h; y++) {
                        if (wall) {
                            boolean bandWindow = y >= 8 && y <= 9 && (x + z) % 4 != 0;
                            boolean door = z == -hw && Math.abs(x) <= 1 && y <= 3;
                            p.set(cx + x, y, cz + z, door ? AIR : bandWindow ? ModBlocks.SHATTERED_GLASS.get().defaultBlockState() : concrete(r));
                        } else {
                            p.set(cx + x, y, cz + z, AIR);
                        }
                    }
                }
            }
            // containment dome, blown open on one side
            double ba = r.nextDouble() * Math.PI * 2;
            int R = 10;
            for (int x = -R; x <= R; x++) {
                for (int z = -R; z <= R; z++) {
                    for (int y = 0; y <= R; y++) {
                        double d = Math.sqrt(x * x + y * y + z * z);
                        if (d > R + 0.5 || d < R - 0.6) continue;
                        double ang = Math.atan2(z, x), da = Math.abs(Math.atan2(Math.sin(ang - ba), Math.cos(ang - ba)));
                        if (y > 3 && da < 0.9 + r.nextDouble() * 0.2) continue;
                        if (y > 8 && r.nextInt(5) == 0) continue;
                        p.set(cx + x, h + y, cz + z, y < 2 ? ModBlocks.SCORCHED_CONCRETE.get().defaultBlockState() : concrete(r));
                    }
                }
            }
            // the core: a ruptured vessel in a flooded pit, rebar cage, fuel racks
            for (int x = -6; x <= 6; x++) {
                for (int z = -6; z <= 6; z++) {
                    for (int y = -3; y <= -1; y++) p.set(cx + x, y, cz + z, AIR);
                    p.set(cx + x, -4, cz + z, ModBlocks.SCORCHED_CONCRETE.get().defaultBlockState());
                    p.set(cx + x, -3, cz + z, ModBlocks.TOXIC_SLUDGE.get().defaultBlockState());
                }
            }
            // what is left of the fuel: a slumped mass of cooled corium at the bottom of the vessel, hot enough to kill
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (Math.abs(x) + Math.abs(z) <= 1 || r.nextBoolean()) p.set(cx + x, -4, cz + z, GB.block("cooled_corium"));
                }
            }
            p.set(cx, -3, cz, GB.block("cooled_corium"));
            for (int y = -3; y <= 9; y++) {
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        boolean shell = Math.abs(x) == 2 || Math.abs(z) == 2;
                        if (!shell || (Math.abs(x) == 2 && Math.abs(z) == 2)) continue;
                        if (y > 5 && r.nextInt(3) == 0) continue; // torn top
                        p.set(cx + x, y, cz + z, (x + y + z) % 4 == 0 ? rebar() : plate());
                    }
                }
            }
            for (int y = -3; y <= 2; y++) {
                for (int i = -5; i <= 5; i += 2) {
                    p.set(cx + i, y, cz + 5, rebar());
                    p.set(cx + i, y, cz - 5, rebar());
                }
            }
            p.chest(cx + 7, 0, cz + 7, Loot.VAULT);
        }

        // ------------------------------------------------------------------ turbine hall
        private void turbineHall(Plan p, RandomSource r) {
            int x0 = -4, x1 = 30, z0 = 14, z1 = 27, h = 11;
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    boolean wall = x == x0 || x == x1 || z == z0 || z == z1;
                    for (int y = 0; y <= h; y++) {
                        if (!wall) {
                            p.set(x, y, z, AIR);
                            continue;
                        }
                        boolean pier = (x - x0) % 5 == 0 || z == z0 && false;
                        boolean window = y >= 5 && y <= 8 && !pier && (z == z0 || z == z1);
                        boolean door = (x == x0 || x == x1) && Math.abs(z - (z0 + z1) / 2) <= 2 && y <= 5;
                        if (door) p.set(x, y, z, AIR);
                        else if (window) p.set(x, y, z, r.nextInt(3) == 0 ? ModBlocks.SHATTERED_GLASS.get().defaultBlockState() : AIR);
                        else p.set(x, y, z, pier ? concrete(r) : brick(r));
                    }
                }
            }
            // corrugated roof on steel trusses, rusted through in places
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    if (r.nextInt(7) == 0 && x > x0 + 2 && x < x1 - 2) continue;
                    p.set(x, h + 1, z, GB.block("rusted_corrugated_steel_slab").setValue(SlabBlock.TYPE, SlabType.BOTTOM));
                    if ((x - x0) % 5 == 0) p.set(x, h, z, rebar());
                }
            }
            // two turbine sets lying along the hall: casings of plating with rebar shafts
            for (int t = 0; t < 2; t++) {
                int sx = x0 + 4 + t * 14, cz = (z0 + z1) / 2;
                for (int x = sx; x < sx + 10; x++) {
                    for (int y = 1; y <= 4; y++) {
                        for (int z = cz - 2; z <= cz + 2; z++) {
                            double d = Math.sqrt((y - 2.5) * (y - 2.5) + (z - cz) * (z - cz));
                            if (d <= 2.2 && (d > 1.2 || x == sx || x == sx + 9)) p.set(x, y, z, plate());
                        }
                    }
                    p.set(x, 0, cz - 3, concrete(r));
                    p.set(x, 0, cz + 3, concrete(r));
                }
            }
            // overhead crane rail
            for (int x = x0 + 1; x < x1; x++) {
                p.set(x, h - 1, z0 + 2, plate());
                p.set(x, h - 1, z1 - 2, plate());
            }
            p.chest(x1 - 2, 0, z1 - 2, Loot.BUNKER);
        }

        // ------------------------------------------------------------------ control block
        private void controlBlock(Plan p, RandomSource r) {
            int x0 = 18, x1 = 32, z0 = -27, z1 = -15, fh = 5;
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    boolean wall = x == x0 || x == x1 || z == z0 || z == z1;
                    for (int y = 0; y <= fh * 2; y++) {
                        if (y == fh || y == fh * 2) {
                            p.set(x, y, z, concrete(r));
                        } else if (!wall) {
                            p.set(x, y, z, AIR);
                        } else {
                            int ly = y % fh;
                            boolean window = (ly == 2 || ly == 3) && ((x - x0) % 3 != 0) && (z == z0 || z == z1);
                            boolean door = z == z1 && x == (x0 + x1) / 2 && y <= 2 && y > 0;
                            p.set(x, y, z, door ? AIR : window ? (r.nextInt(3) == 0 ? ModBlocks.SHATTERED_GLASS.get().defaultBlockState() : Blocks.IRON_BARS.defaultBlockState()) : concrete(r));
                        }
                    }
                }
            }
            for (int y = 1; y <= fh * 2 - 1; y++) {
                p.set(x0 + 1, y, z0 + 1, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.EAST));
            }
            // control desks: slabs and plating panels in a horseshoe
            for (int x = x0 + 3; x <= x1 - 3; x++) {
                p.set(x, fh + 1, z0 + 2, plate());
                p.set(x, fh + 1, z0 + 3, ModBlocks.SCORCHED_CONCRETE_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
            }
            for (int y = fh + 2; y <= fh + 3; y++) {
                for (int x = x0 + 3; x <= x1 - 3; x += 2) p.set(x, y, z0 + 1, ModBlocks.SHATTERED_GLASS.get().defaultBlockState());
            }
            p.chest(x1 - 1, 1, z0 + 1, Loot.BUNKER);
            p.chest(x0 + 3, fh + 1, z0 + 4, Loot.BUNKER);
            p.spawners.add(p.at((x0 + x1) / 2, 1, (z0 + z1) / 2));
            // sandbag post by the door
            for (int x = (x0 + x1) / 2 - 3; x <= (x0 + x1) / 2 + 3; x++) {
                if (Math.abs(x - (x0 + x1) / 2) <= 1) continue;
                p.set(x, 0, z1 + 3, ModBlocks.SANDBAGS.get().defaultBlockState());
            }
        }

        // ------------------------------------------------------------------ flue stack
        private void flueStack(Plan p, RandomSource r, int cx, int cz) {
            int H = 40 + r.nextInt(12);
            for (int y = 0; y < H; y++) {
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        boolean corner = Math.abs(x) == 2 && Math.abs(z) == 2;
                        boolean shell = Math.abs(x) == 2 || Math.abs(z) == 2;
                        if (corner) continue;
                        if (!shell) {
                            p.set(cx + x, y, cz + z, AIR);
                            continue;
                        }
                        BlockState s = brick(r);
                        if (y % 10 == 9) s = ModBlocks.SCORCHED_CONCRETE.get().defaultBlockState(); // bands
                        if (y > H - 5 && r.nextInt(3) == 0) s = ModBlocks.CHARRED_PLANKS.get().defaultBlockState(); // soot
                        p.set(cx + x, y, cz + z, s);
                    }
                }
            }
            for (int y = 1; y < H; y++) p.set(cx - 3, y, cz, rebar());
        }

        // ------------------------------------------------------------------ pipe runs
        private void pipe(Plan p, int x0, int z0, int x1, int z1, int y) {
            int dx = Integer.signum(x1 - x0), dz = Integer.signum(z1 - z0);
            int x = x0, z = z0, i = 0;
            while (true) {
                p.set(x, y, z, plate());
                if (i % 5 == 0) for (int k = 0; k < y; k++) if (p.get(x, k, z) == null) p.set(x, k, z, rebar());
                if (x == x1 && z == z1) break;
                if (x != x1) x += dx;
                else z += dz;
                i++;
            }
        }

        private void pipes(Plan p, RandomSource r) {
            pipe(p, -9, -6, -1, -6, 6);     // tower A to reactor
            pipe(p, -9, 9, -1, 9, 6);       // tower B to reactor
            pipe(p, -9, -6, -9, 9, 6);      // header between towers
            pipe(p, 10, 11, 10, 13, 8);     // reactor to turbine hall
            pipe(p, 21, -1, 31, -1, 4);     // reactor to flue
            pipe(p, 25, -14, 25, -4, 3);    // control block conduit
        }

        // ------------------------------------------------------------------ fence and debris
        private void fence(Plan p, RandomSource r) {
            for (int x = -PX; x <= PX; x++) {
                for (int z = -PZ; z <= PZ; z++) {
                    boolean edge = Math.abs(x) == PX || Math.abs(z) == PZ;
                    if (!edge) continue;
                    if (z == -PZ && Math.abs(x - 20) <= 3) continue; // main gate
                    if (r.nextInt(6) == 0) continue; // torn down
                    p.set(x, 0, z, rebar());
                    if (r.nextInt(3) != 0) p.set(x, 1, z, rebar());
                }
            }
            // gatehouse
            for (int y = 0; y <= 3; y++) {
                for (int x = 25; x <= 28; x++) {
                    for (int z = -PZ - 0; z <= -PZ + 3; z++) {
                        boolean wall = x == 25 || x == 28 || z == -PZ || z == -PZ + 3;
                        p.set(x, y, z, y == 3 ? concrete(r) : wall ? (y == 1 && r.nextBoolean() ? Blocks.IRON_BARS.defaultBlockState() : concrete(r)) : AIR);
                    }
                }
            }
            p.set(25, 1, -PZ + 2, AIR);
            p.set(25, 0, -PZ + 2, AIR);
        }

        private void debris(Plan p, RandomSource r) {
            for (int i = 0; i < 70; i++) {
                int x = -PX + 1 + r.nextInt(PX * 2 - 1), z = -PZ + 1 + r.nextInt(PZ * 2 - 1);
                if (p.get(x, 0, z) != null) continue;
                int k = r.nextInt(10);
                BlockState s = k < 4 ? ModBlocks.RUBBLE.get().defaultBlockState() : k < 6 ? ModBlocks.SCRAP_PILE.get().defaultBlockState()
                        : k < 7 ? ModBlocks.TOXIC_SLUDGE.get().defaultBlockState() : k < 9 ? GB.block("fallout_ash") : ModBlocks.BURNT_TIRE.get().defaultBlockState();
                p.set(x, 0, z, s);
            }
            // a few drums of waste: plating stacks with sludge seeping out
            for (int i = 0; i < 14; i++) {
                int x = 2 + r.nextInt(12), z = -28 + r.nextInt(8);
                if (p.get(x, 0, z) != null) continue;
                p.set(x, 0, z, GB.block("waste_drum"));
                if (r.nextBoolean()) p.set(x, 1, z, GB.block("waste_drum"));
                if (p.get(x + 1, 0, z) == null) p.set(x + 1, 0, z, ModBlocks.TOXIC_SLUDGE.get().defaultBlockState());
            }
        }
    }
}
