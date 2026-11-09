package com.lach.affermath.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

/** Piece that rebuilds its {@link Plan} from (origin, rotation, seed, variant) and writes the part inside each chunk. */
public abstract class PlannedPiece extends StructurePiece {
    protected final BlockPos origin;
    protected final Rotation rotation;
    protected final long seed;
    protected final int variant;
    private Plan plan;

    protected PlannedPiece(StructurePieceType type, BlockPos origin, Rotation rotation, long seed, int variant) {
        super(type, 0, new BoundingBox(origin));
        this.origin = origin;
        this.rotation = rotation;
        this.seed = seed;
        this.variant = variant;
        this.boundingBox = plan().box();
    }

    protected PlannedPiece(StructurePieceType type, CompoundTag tag) {
        super(type, tag);
        this.origin = tag.read("Origin", BlockPos.CODEC).orElse(BlockPos.ZERO);
        this.rotation = Rotation.values()[tag.getIntOr("Rot", 0) & 3];
        this.seed = tag.getLongOr("Seed", 0L);
        this.variant = tag.getIntOr("Variant", 0);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.store("Origin", BlockPos.CODEC, origin);
        tag.putInt("Rot", rotation.ordinal());
        tag.putLong("Seed", seed);
        tag.putInt("Variant", variant);
    }

    protected Plan plan() {
        if (plan == null) {
            plan = new Plan(origin, rotation);
            build(plan, RandomSource.create(seed));
        }
        return plan;
    }

    protected abstract void build(Plan plan, RandomSource rand);

    protected void onWrite(WorldGenLevel level, Plan plan, BoundingBox chunkBox) {
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator gen, RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        Plan p = plan();
        BlockState fill = Blocks.COBBLESTONE.defaultBlockState();
        for (BlockPos f : p.foundations) {
            if (!chunkBox.isInside(f)) continue;
            BlockPos.MutableBlockPos m = f.mutable().move(Direction.DOWN);
            for (int i = 0; i < 12 && m.getY() > level.getMinY(); i++, m.move(Direction.DOWN)) {
                BlockState s = level.getBlockState(m);
                if (!s.canBeReplaced() && s.getFluidState().isEmpty()) break;
                level.setBlock(m, i == 0 ? com.lach.affermath.registry.ModBlocks.RUBBLE.get().defaultBlockState() : fill, 2);
            }
        }
        for (var e : p.blocks.entrySet()) {
            if (chunkBox.isInside(e.getKey())) level.setBlock(e.getKey(), e.getValue(), 2);
        }
        for (var e : p.chests.entrySet()) {
            BlockPos pos = e.getKey();
            if (!chunkBox.isInside(pos)) continue;
            level.setBlock(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,
                    rotation.rotate(Direction.Plane.HORIZONTAL.getRandomDirection(RandomSource.create(seed ^ pos.asLong())))), 2);
            RandomizableContainer.setBlockEntityLootTable(level, random, pos, e.getValue());
        }
        for (BlockPos pos : p.spawners) {
            if (!chunkBox.isInside(pos)) continue;
            level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof net.minecraft.world.level.block.entity.SpawnerBlockEntity sp) {
                sp.setEntityId(com.lach.affermath.registry.ModEntities.HOLLOW_SOLDIER.get(), random);
            }
        }
        onWrite(level, p, chunkBox);
    }
}
