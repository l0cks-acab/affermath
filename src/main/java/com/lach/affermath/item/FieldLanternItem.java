package com.lach.affermath.item;

import com.lach.affermath.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;

public class FieldLanternItem extends CurioItem {
    public FieldLanternItem(Properties props) {
        super(props);
    }

    @Override
    public void curioTick(SlotContext ctx, ItemStack stack) {
        LivingEntity e = ctx.entity();
        Level level = e.level();
        if (level.isClientSide || e.tickCount % 4 != 0 || e.isSpectator()) return;
        BlockPos pos = BlockPos.containing(e.getX(), e.getEyeY(), e.getZ());
        if (level.getBlockState(pos).isAir() && !level.getBlockState(pos).is(ModBlocks.FIELD_LIGHT.get())) {
            level.setBlock(pos, ModBlocks.FIELD_LIGHT.get().defaultBlockState(), 3);
        }
    }
}
