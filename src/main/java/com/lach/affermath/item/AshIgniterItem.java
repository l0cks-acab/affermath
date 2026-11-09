package com.lach.affermath.item;

import com.lach.affermath.block.PortalHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class AshIgniterItem extends Item {
    public AshIgniterItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
        if (!PortalHelper.isFrame(level.getBlockState(ctx.getClickedPos()))) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (PortalHelper.tryLight(level, pos)) {
            level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1f, 0.6f);
            level.playSound(null, pos, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 0.4f, 0.5f);
            if (ctx.getPlayer() != null) {
                ctx.getItemInHand().hurtAndBreak(1, ctx.getPlayer(), LivingEntity.getSlotForHand(ctx.getHand()));
            }
            return InteractionResult.CONSUME;
        }
        level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1f, 0.5f);
        return InteractionResult.FAIL;
    }
}
