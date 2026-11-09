package com.lach.affermath.item;

import com.lach.affermath.Affermath;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.SmithingTemplateItem;

import java.util.List;

public final class WarforgedTemplate {
    private static Identifier mc(String p) {
        return Identifier.withDefaultNamespace(p);
    }

    public static SmithingTemplateItem create(net.minecraft.world.item.Item.Properties props) {
        String k = "item." + Affermath.MODID + ".smithing_template.warforged.";
        return new SmithingTemplateItem(
                Component.translatable(k + "applies_to").withStyle(ChatFormatting.BLUE),
                Component.translatable(k + "ingredients").withStyle(ChatFormatting.BLUE),
                Component.translatable(k + "base_slot"),
                Component.translatable(k + "additions_slot"),
                List.of(mc("container/slot/helmet"), mc("container/slot/sword"), mc("container/slot/chestplate"),
                        mc("container/slot/pickaxe"), mc("container/slot/leggings"), mc("container/slot/axe"),
                        mc("container/slot/boots"), mc("container/slot/hoe"), mc("container/slot/shovel")),
                List.of(mc("container/slot/ingot")), props);
    }

    private WarforgedTemplate() {}
}
