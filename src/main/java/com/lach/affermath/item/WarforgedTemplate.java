package com.lach.affermath.item;

import com.lach.affermath.Affermath;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.SmithingTemplateItem;

import java.util.List;

public final class WarforgedTemplate {
    private static ResourceLocation mc(String p) {
        return ResourceLocation.withDefaultNamespace(p);
    }

    public static SmithingTemplateItem create() {
        String k = "item." + Affermath.MODID + ".smithing_template.warforged.";
        return new SmithingTemplateItem(
                Component.translatable(k + "applies_to").withStyle(ChatFormatting.BLUE),
                Component.translatable(k + "ingredients").withStyle(ChatFormatting.BLUE),
                Component.translatable(k + "upgrade").withStyle(ChatFormatting.GRAY),
                Component.translatable(k + "base_slot"),
                Component.translatable(k + "additions_slot"),
                List.of(mc("item/empty_armor_slot_helmet"), mc("item/empty_slot_sword"), mc("item/empty_armor_slot_chestplate"),
                        mc("item/empty_slot_pickaxe"), mc("item/empty_armor_slot_leggings"), mc("item/empty_slot_axe"),
                        mc("item/empty_armor_slot_boots"), mc("item/empty_slot_hoe"), mc("item/empty_slot_shovel")),
                List.of(mc("item/empty_slot_ingot")));
    }

    private WarforgedTemplate() {}
}
