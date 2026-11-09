package com.lach.affermath.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

public final class CurioHelper {
    public static boolean has(LivingEntity e, Item item) {
        return !find(e, item).isEmpty();
    }

    public static ItemStack find(LivingEntity e, Item item) {
        return CuriosApi.getCuriosInventory(e)
                .flatMap(inv -> inv.findFirstCurio(item))
                .map(r -> r.stack())
                .orElse(ItemStack.EMPTY);
    }

    private CurioHelper() {}
}
