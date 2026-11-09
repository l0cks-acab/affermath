package com.lach.affermath.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import top.theillusivec4.curios.api.SlotContext;

/** Eats the best-fitting safe food from the inventory whenever the hunger bar drops. */
public class RationTinItem extends CurioItem {
    public RationTinItem(Properties props) {
        super(props);
    }

    @Override
    public void curioTick(SlotContext ctx, ItemStack tin) {
        if (!(ctx.entity() instanceof Player player) || player.level().isClientSide() || player.tickCount % 20 != 0) return;
        if (player.isCreative() || player.isSpectator()) return;
        FoodData food = player.getFoodData();
        int missing = 20 - food.getFoodLevel();
        if (missing <= 0) return;
        Inventory inv = player.getInventory();
        int best = -1, bestNut = -1, small = -1, smallNut = Integer.MAX_VALUE;
        for (int i = 0; i < inv.getNonEquipmentItems().size(); i++) {
            ItemStack s = inv.getNonEquipmentItems().get(i);
            FoodProperties fp = s.get(DataComponents.FOOD);
            if (fp == null || !safe(s, fp)) continue;
            int n = fp.nutrition();
            if (n <= missing && n > bestNut) { best = i; bestNut = n; }
            if (n < smallNut) { small = i; smallNut = n; }
        }
        if (best < 0 && food.getFoodLevel() <= 6) best = small;
        if (best < 0) return;
        ItemStack stack = inv.getNonEquipmentItems().get(best);
        ItemStack result = stack.finishUsingItem(player.level(), player);
        if (result != stack) {
            if (stack.isEmpty()) inv.getNonEquipmentItems().set(best, result);
            else if (!result.isEmpty() && !inv.add(result)) player.drop(result, false);
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BURP,
                SoundSource.PLAYERS, 0.5f, player.getRandom().nextFloat() * 0.1f + 0.9f);
    }

    private static boolean safe(ItemStack s, FoodProperties fp) {
        if (s.is(Items.CHORUS_FRUIT) || s.is(Items.SUSPICIOUS_STEW) || s.is(Items.ENCHANTED_GOLDEN_APPLE) || s.is(Items.GOLDEN_APPLE)) return false;
        var cons = s.get(DataComponents.CONSUMABLE);
        if (cons == null) return true;
        for (var e : cons.onConsumeEffects()) {
            if (e instanceof net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect a) {
                for (var m : a.effects()) if (!m.getEffect().value().isBeneficial()) return false;
            } else if (!(e instanceof net.minecraft.world.item.consume_effects.PlaySoundConsumeEffect)) return false;
        }
        return true;
    }
}
