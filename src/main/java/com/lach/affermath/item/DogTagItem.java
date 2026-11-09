package com.lach.affermath.item;

import com.lach.affermath.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** A single tag pulled from a fallen soldier. Click it onto Dog Tags to string it on. */
public class DogTagItem extends Item {
    private static final String[] FIRST = {"A.", "B.", "C.", "D.", "E.", "F.", "G.", "H.", "J.", "K.", "L.", "M.", "N.", "O.", "P.", "R.", "S.", "T.", "V.", "W."};
    private static final String[] LAST = {"Ashby", "Bellamy", "Carrow", "Dunmore", "Ellery", "Fenwick", "Garrick", "Hale", "Irving", "Jessop",
            "Kettering", "Lowell", "Marsh", "Northcott", "Orme", "Pryce", "Quill", "Rudd", "Stroud", "Thorne", "Underhill", "Vance",
            "Whitlock", "Yardley", "Ainsworth", "Blakely", "Corbin", "Drury", "Emsworth", "Fairley", "Grimsby", "Holloway"};

    public DogTagItem(Properties props) {
        super(props);
    }

    public static String randomName(RandomSource r) {
        int num = 10000 + r.nextInt(89999);
        return FIRST[r.nextInt(FIRST.length)] + " " + LAST[r.nextInt(LAST.length)] + " #" + num;
    }

    public static String nameOf(ItemStack stack) {
        String n = stack.get(ModItems.SOLDIER.get());
        return n == null ? "Unknown Soldier" : n;
    }

    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level, Entity entity, @org.jetbrains.annotations.Nullable net.minecraft.world.entity.EquipmentSlot slot) {
        if (!stack.has(ModItems.SOLDIER.get())) {
            if (stack.getCount() == 1) stack.set(ModItems.SOLDIER.get(), randomName(level.getRandom()));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tip, TooltipFlag flag) {
        if (stack.has(ModItems.SOLDIER.get())) tip.accept(Component.literal(nameOf(stack)).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tip.accept(ModItems.tip("dog_tag"));
    }
}
