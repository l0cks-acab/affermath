package com.lach.affermath.registry;

import com.lach.affermath.Affermath;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

public final class ModArmor {
    public static final DeferredRegister<ArmorMaterial> MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, Affermath.MODID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> WARFORGED = MATERIALS.register("warforged", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
                m.put(ArmorItem.Type.BOOTS, 3);
                m.put(ArmorItem.Type.LEGGINGS, 6);
                m.put(ArmorItem.Type.CHESTPLATE, 8);
                m.put(ArmorItem.Type.HELMET, 3);
                m.put(ArmorItem.Type.BODY, 11);
            }), 15, SoundEvents.ARMOR_EQUIP_NETHERITE, () -> Ingredient.of(ModItems.WARFORGED_PLATING.get()),
            List.of(new ArmorMaterial.Layer(Affermath.id("warforged"))), 3.0f, 0.1f));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> GAS_MASK = MATERIALS.register("gas_mask", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
                m.put(ArmorItem.Type.BOOTS, 1);
                m.put(ArmorItem.Type.LEGGINGS, 2);
                m.put(ArmorItem.Type.CHESTPLATE, 3);
                m.put(ArmorItem.Type.HELMET, 1);
                m.put(ArmorItem.Type.BODY, 3);
            }), 12, SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(Items.LEATHER),
            List.of(new ArmorMaterial.Layer(Affermath.id("gas_mask"))), 0f, 0f));

    public static final Tier WARFORGED_TIER = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2031, 9.0f, 4.0f, 15,
            () -> Ingredient.of(ModItems.WARFORGED_PLATING.get()));

    private ModArmor() {}
}
