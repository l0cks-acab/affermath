package com.lach.affermath.world;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.*;

/** Deterministic block layout for a structure, in world coordinates. */
public final class Plan {
    public final BlockPos origin;
    public final Rotation rot;
    public final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    public final Map<BlockPos, ResourceKey<LootTable>> chests = new LinkedHashMap<>();
    public final List<BlockPos> spawners = new ArrayList<>();
    public final Set<BlockPos> foundations = new LinkedHashSet<>();
    public BlockPos boss;

    public Plan(BlockPos origin, Rotation rot) {
        this.origin = origin;
        this.rot = rot;
    }

    public BlockPos at(int dx, int dy, int dz) {
        return origin.offset(new BlockPos(dx, dy, dz).rotate(rot));
    }

    public void set(int dx, int dy, int dz, BlockState s) {
        blocks.put(at(dx, dy, dz), s.rotate(rot));
    }

    public BlockState get(int dx, int dy, int dz) {
        return blocks.get(at(dx, dy, dz));
    }

    public void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState s) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++)
            for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++)
                for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) set(x, y, z, s);
    }

    public void chest(int dx, int dy, int dz, ResourceKey<LootTable> loot) {
        BlockPos p = at(dx, dy, dz);
        blocks.remove(p);
        chests.put(p, loot);
    }

    public void foundation(int dx, int dz) {
        BlockPos p = at(dx, 0, dz);
        foundations.add(new BlockPos(p.getX(), origin.getY(), p.getZ()));
    }

    public BoundingBox box() {
        List<BlockPos> all = new ArrayList<>(blocks.keySet());
        all.addAll(chests.keySet());
        all.addAll(spawners);
        if (boss != null) all.add(boss);
        BoundingBox b = BoundingBox.encapsulatingPositions(all).orElse(new BoundingBox(origin));
        if (!foundations.isEmpty()) b = new BoundingBox(b.minX(), b.minY() - 10, b.minZ(), b.maxX(), b.maxY(), b.maxZ());
        return b;
    }
}
