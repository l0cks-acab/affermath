package com.lach.affermath.compat;

import com.lach.affermath.Affermath;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** GroundBurst integration. GB is a required dependency, looked up by id so nothing links against its classes. */
public final class GB {
    public static final String ID = "groundburst";
    /** Head pieces that filter the air: the Affermath gas mask and GB's hazmat hoods. */
    public static final TagKey<Item> GAS_MASKS = TagKey.create(Registries.ITEM, Affermath.id("gas_masks"));

    public static boolean masked(LivingEntity e) {
        return e.getItemBySlot(EquipmentSlot.HEAD).is(GAS_MASKS);
    }

    public static BlockState block(String name) {
        Block b = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ID, name));
        return b.defaultBlockState();
    }

    private GB() {}
}
