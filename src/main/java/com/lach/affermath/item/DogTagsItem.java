package com.lach.affermath.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.lach.affermath.Affermath;
import com.lach.affermath.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.SlotContext;

import java.util.ArrayList;
import java.util.List;

/** Necklace curio. Every tag strung on it adds armour. */
public class DogTagsItem extends CurioItem {
    public static final int MAX_TAGS = 8;

    public DogTagsItem(Properties props) {
        super(props);
    }

    public static List<String> tags(ItemStack stack) {
        List<String> l = stack.get(ModItems.TAG_NAMES.get());
        return l == null ? List.of() : l;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack me, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess carried) {
        if (action != ClickAction.PRIMARY || !other.is(ModItems.DOG_TAG.get())) return false;
        List<String> cur = tags(me);
        if (cur.size() >= MAX_TAGS) return false;
        List<String> next = new ArrayList<>(cur);
        next.add(DogTagItem.nameOf(other));
        me.set(ModItems.TAG_NAMES.get(), List.copyOf(next));
        other.shrink(1);
        player.playSound(SoundEvents.CHAIN_PLACE, 0.8f, 1.2f);
        return true;
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext ctx, Identifier id, ItemStack stack) {
        Multimap<Holder<Attribute>, AttributeModifier> m = HashMultimap.create();
        int n = Math.max(1, tags(stack).size());
        m.put(Attributes.ARMOR, new AttributeModifier(Affermath.id("dog_tags"), n, AttributeModifier.Operation.ADD_VALUE));
        if (n >= 4) m.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Affermath.id("dog_tags_toughness"), (n - 3) * 0.5, AttributeModifier.Operation.ADD_VALUE));
        return m;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tip, TooltipFlag flag) {
        List<String> names = tags(stack);
        tip.accept(Component.translatable("tooltip.affermath.dog_tags.count", names.size(), MAX_TAGS).withStyle(ChatFormatting.GRAY));
        for (String n : names) tip.accept(Component.literal("  " + n).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tip.accept(ModItems.tip("dog_tags"));
    }
}
