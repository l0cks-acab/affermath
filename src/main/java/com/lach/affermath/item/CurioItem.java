package com.lach.affermath.item;

import com.lach.affermath.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

/** Base for Affermath curios: one per slot, tooltip from lang. */
public class CurioItem extends Item implements ICurioItem {
    public CurioItem(Properties props) {
        super(props);
    }

    @Override
    public boolean canEquipFromUse(SlotContext ctx, ItemStack stack) {
        return true;
    }

    @Override
    public boolean canEquip(SlotContext ctx, ItemStack stack) {
        return CurioHelper.find(ctx.entity(), this).isEmpty() || CurioHelper.find(ctx.entity(), this) == stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag flag) {
        tip.add(ModItems.tip(BuiltInRegistries.ITEM.getKey(this).getPath()));
    }
}
