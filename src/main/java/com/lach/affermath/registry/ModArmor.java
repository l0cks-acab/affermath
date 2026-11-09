package com.lach.affermath.registry;

import com.lach.affermath.Affermath;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;
import java.util.Map;

public final class ModArmor {
    public static final TagKey<Item> WARFORGED_REPAIR = TagKey.create(Registries.ITEM, Affermath.id("warforged_repair"));
    public static final ResourceKey<EquipmentAsset> WARFORGED_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, Affermath.id("warforged"));
    public static final ResourceKey<EquipmentAsset> GAS_MASK_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, Affermath.id("gas_mask"));

    private static Map<ArmorType, Integer> defense(int boots, int legs, int chest, int helm, int body) {
        Map<ArmorType, Integer> m = new EnumMap<>(ArmorType.class);
        m.put(ArmorType.BOOTS, boots);
        m.put(ArmorType.LEGGINGS, legs);
        m.put(ArmorType.CHESTPLATE, chest);
        m.put(ArmorType.HELMET, helm);
        m.put(ArmorType.BODY, body);
        return m;
    }

    public static final ArmorMaterial WARFORGED = new ArmorMaterial(37, defense(3, 6, 8, 3, 11), 15,
            SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0f, 0.1f, WARFORGED_REPAIR, WARFORGED_ASSET);
    public static final ArmorMaterial GAS_MASK = new ArmorMaterial(12, defense(1, 2, 3, 1, 3), 12,
            SoundEvents.ARMOR_EQUIP_LEATHER, 0f, 0f, ItemTags.REPAIRS_LEATHER_ARMOR, GAS_MASK_ASSET);

    public static final ToolMaterial WARFORGED_TOOL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2031, 9.0f, 4.0f, 15, WARFORGED_REPAIR);

    private ModArmor() {}
}
