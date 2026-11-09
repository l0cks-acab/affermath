package com.lach.affermath.block;

import com.lach.affermath.item.CurioHelper;
import com.lach.affermath.registry.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Invisible light left by a Field Lantern wearer; removes itself once nobody wearing one is close. */
public class FieldLightBlock extends Block {
    public static final MapCodec<FieldLightBlock> CODEC = simpleCodec(FieldLightBlock::new);

    public FieldLightBlock(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.empty();
    }

    @Override
    protected void onPlace(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, BlockState old, boolean moved) {
        level.scheduleTick(pos, this, 10);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean keep = false;
        for (Player p : level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(2.5))) {
            if (CurioHelper.has(p, ModItems.FIELD_LANTERN.get())) {
                keep = true;
                break;
            }
        }
        if (keep) level.scheduleTick(pos, this, 10);
        else level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }
}
