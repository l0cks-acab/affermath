package com.lach.affermath.block;

import com.lach.affermath.Affermath;
import com.lach.affermath.registry.ModBlocks;
import com.lach.affermath.registry.ModWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class PortalHelper {
    public static final TagKey<Block> FRAME = TagKey.create(Registries.BLOCK, Affermath.id("portal_frame"));
    private static final int MAX = 21;

    public static boolean isFrame(BlockState s) {
        return s.is(FRAME);
    }

    private static boolean isInner(BlockState s) {
        return s.isAir() || s.is(Blocks.FIRE) || s.is(ModBlocks.FIELD_LIGHT.get());
    }

    /** Try to light a frame containing {@code start}. Returns true if a portal was created. */
    public static boolean tryLight(Level level, BlockPos start) {
        for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            List<BlockPos> inner = findInner(level, start, axis);
            if (inner != null) {
                BlockState st = ModBlocks.PORTAL.get().defaultBlockState().setValue(AffermathPortalBlock.AXIS, axis);
                for (BlockPos p : inner) level.setBlock(p, st, 18);
                return true;
            }
        }
        return false;
    }

    private static List<BlockPos> findInner(Level level, BlockPos start, Direction.Axis axis) {
        if (!isInner(level.getBlockState(start))) return null;
        Direction right = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        Direction left = right.getOpposite();
        BlockPos p = start;
        int n = 0;
        while (isInner(level.getBlockState(p.below())) && n++ < MAX) p = p.below();
        if (!isFrame(level.getBlockState(p.below()))) return null;
        n = 0;
        while (isInner(level.getBlockState(p.relative(left))) && n++ < MAX) p = p.relative(left);
        if (!isFrame(level.getBlockState(p.relative(left)))) return null;
        BlockPos corner = p;
        int w = 0;
        while (w <= MAX && isInner(level.getBlockState(corner.relative(right, w)))) w++;
        if (w < 2 || w > MAX || !isFrame(level.getBlockState(corner.relative(right, w)))) return null;
        int h = 0;
        while (h <= MAX && isInner(level.getBlockState(corner.above(h)))) h++;
        if (h < 3 || h > MAX || !isFrame(level.getBlockState(corner.above(h)))) return null;
        List<BlockPos> out = new ArrayList<>();
        for (int x = 0; x < w; x++) {
            if (!isFrame(level.getBlockState(corner.relative(right, x).below()))) return null;
            if (!isFrame(level.getBlockState(corner.relative(right, x).above(h)))) return null;
            for (int y = 0; y < h; y++) {
                BlockPos q = corner.relative(right, x).above(y);
                if (!isInner(level.getBlockState(q))) return null;
                out.add(q.immutable());
            }
        }
        for (int y = 0; y < h; y++) {
            if (!isFrame(level.getBlockState(corner.relative(left).above(y)))) return null;
            if (!isFrame(level.getBlockState(corner.relative(right, w).above(y)))) return null;
        }
        return out;
    }

    public static TeleportTransition destination(ServerLevel level, Entity entity, BlockPos from) {
        ServerLevel target = level.getServer().getLevel(Affermath.isAffermath(level) ? Level.OVERWORLD : Affermath.LEVEL);
        if (target == null) return null;
        BlockPos guess = new BlockPos(from.getX(), from.getY(), from.getZ());
        guess = target.getWorldBorder().clampToBounds(guess.getX(), guess.getY(), guess.getZ());
        BlockPos dest = findExisting(target, guess).orElseGet(() -> null);
        if (dest == null) dest = build(target, guess);
        return new TeleportTransition(target, Vec3.atBottomCenterOf(dest.south()), entity.getDeltaMovement(), entity.getYRot(),
                entity.getXRot(), TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET));
    }

    private static Optional<BlockPos> findExisting(ServerLevel level, BlockPos near) {
        PoiManager poi = level.getPoiManager();
        int r = 48;
        poi.ensureLoadedAndValid(level, near, r);
        return poi.getInSquare(h -> h.is(ModWorld.PORTAL_POI.getKey()), near, r, PoiManager.Occupancy.ANY)
                .map(PoiRecord::getPos)
                .filter(p -> level.getBlockState(p).is(ModBlocks.PORTAL.get()))
                .min(Comparator.<BlockPos>comparingDouble(p -> p.distSqr(near)).thenComparingInt(BlockPos::getY))
                .map(p -> {
                    BlockPos b = p;
                    while (level.getBlockState(b.below()).is(ModBlocks.PORTAL.get())) b = b.below();
                    return b;
                });
    }

    /** Builds a 4x5 scorched brick frame with a small platform and returns the bottom inner portal block. */
    private static BlockPos build(ServerLevel level, BlockPos near) {
        level.getChunk(near.getX() >> 4, near.getZ() >> 4);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, near.getX(), near.getZ());
        y = Math.max(level.getMinY() + 6, Math.min(y, level.getMaxY() - 10));
        BlockPos base = new BlockPos(near.getX(), y, near.getZ());
        BlockState frame = ModBlocks.SCORCHED_BRICKS.get().defaultBlockState();
        BlockState floor = ModBlocks.SCORCHED_CONCRETE.get().defaultBlockState();
        for (int dx = -2; dx <= 3; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                level.setBlockAndUpdate(base.offset(dx, -1, dz), floor);
                for (int dy = 0; dy < 5; dy++) level.setBlockAndUpdate(base.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
            }
        }
        for (int dx = -1; dx <= 2; dx++) {
            for (int dy = -1; dy <= 3; dy++) {
                boolean edge = dx == -1 || dx == 2 || dy == -1 || dy == 3;
                if (edge) level.setBlockAndUpdate(base.offset(dx, dy, 0), frame);
            }
        }
        BlockState portal = ModBlocks.PORTAL.get().defaultBlockState().setValue(AffermathPortalBlock.AXIS, Direction.Axis.X);
        for (int dx = 0; dx <= 1; dx++)
            for (int dy = 0; dy <= 2; dy++) level.setBlock(base.offset(dx, dy, 0), portal, 18);
        return base;
    }

    private PortalHelper() {}
}
