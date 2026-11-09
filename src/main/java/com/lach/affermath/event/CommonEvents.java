package com.lach.affermath.event;

import com.lach.affermath.Affermath;
import com.lach.affermath.item.CurioHelper;
import com.lach.affermath.registry.ModItems;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = Affermath.MODID)
public final class CommonEvents {
    /** Shrapnel Charm: anything that hits you in melee eats a spray of shrapnel. */
    @SubscribeEvent
    public static void shrapnel(LivingIncomingDamageEvent e) {
        if (!(e.getEntity() instanceof Player player) || player.level().isClientSide()) return;
        if (e.getSource().is(DamageTypes.THORNS) || e.getSource().is(DamageTypeTags.IS_EXPLOSION)) return;
        if (!(e.getSource().getDirectEntity() instanceof LivingEntity attacker) || attacker == player) return;
        if (!CurioHelper.has(player, ModItems.SHRAPNEL_CHARM.get())) return;
        attacker.hurtServer((net.minecraft.server.level.ServerLevel) player.level(), player.damageSources().thorns(player), 2f + e.getAmount() * 0.3f);
    }

    private CommonEvents() {}
}
