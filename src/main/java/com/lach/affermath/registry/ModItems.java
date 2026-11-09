package com.lach.affermath.registry;

import com.lach.affermath.Affermath;
import com.lach.affermath.item.*;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Affermath.MODID);
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Affermath.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<String>>> TAG_NAMES = COMPONENTS.registerComponentType("tag_names",
            b -> b.persistent(Codec.STRING.listOf()).networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list())));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SOLDIER = COMPONENTS.registerComponentType("soldier",
            b -> b.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    // blocks
    public static final DeferredItem<BlockItem> ASH_LAYER = ITEMS.registerSimpleBlockItem(ModBlocks.ASH_LAYER);
    public static final DeferredItem<BlockItem> ASH_BLOCK = ITEMS.registerSimpleBlockItem(ModBlocks.ASH_BLOCK);
    public static final DeferredItem<BlockItem> ASHEN_SOIL = ITEMS.registerSimpleBlockItem(ModBlocks.ASHEN_SOIL);
    public static final DeferredItem<BlockItem> RUBBLE = ITEMS.registerSimpleBlockItem(ModBlocks.RUBBLE);
    public static final DeferredItem<BlockItem> SCORCHED_BRICKS = ITEMS.registerSimpleBlockItem(ModBlocks.SCORCHED_BRICKS);
    public static final DeferredItem<BlockItem> CRACKED_SCORCHED_BRICKS = ITEMS.registerSimpleBlockItem(ModBlocks.CRACKED_SCORCHED_BRICKS);
    public static final DeferredItem<BlockItem> SCORCHED_CONCRETE = ITEMS.registerSimpleBlockItem(ModBlocks.SCORCHED_CONCRETE);
    public static final DeferredItem<BlockItem> CRACKED_SCORCHED_CONCRETE = ITEMS.registerSimpleBlockItem(ModBlocks.CRACKED_SCORCHED_CONCRETE);
    public static final DeferredItem<BlockItem> CHARRED_LOG = ITEMS.registerSimpleBlockItem(ModBlocks.CHARRED_LOG);
    public static final DeferredItem<BlockItem> CHARRED_PLANKS = ITEMS.registerSimpleBlockItem(ModBlocks.CHARRED_PLANKS);
    public static final DeferredItem<BlockItem> REBAR = ITEMS.registerSimpleBlockItem(ModBlocks.REBAR);
    public static final DeferredItem<BlockItem> SHATTERED_GLASS = ITEMS.registerSimpleBlockItem(ModBlocks.SHATTERED_GLASS);
    public static final DeferredItem<BlockItem> SANDBAGS = ITEMS.registerSimpleBlockItem(ModBlocks.SANDBAGS);
    public static final DeferredItem<BlockItem> RUSTED_PLATING = ITEMS.registerSimpleBlockItem(ModBlocks.RUSTED_PLATING);
    public static final DeferredItem<BlockItem> SCRAP_PILE = ITEMS.registerSimpleBlockItem(ModBlocks.SCRAP_PILE);
    public static final DeferredItem<BlockItem> BURNT_TIRE = ITEMS.registerSimpleBlockItem(ModBlocks.BURNT_TIRE);
    public static final DeferredItem<BlockItem> TOXIC_SLUDGE = ITEMS.registerSimpleBlockItem(ModBlocks.TOXIC_SLUDGE);
    public static final DeferredItem<BlockItem> ASPHALT = ITEMS.registerSimpleBlockItem(ModBlocks.ASPHALT);
    public static final DeferredItem<BlockItem> CRACKED_ASPHALT = ITEMS.registerSimpleBlockItem(ModBlocks.CRACKED_ASPHALT);
    public static final DeferredItem<BlockItem> ASPHALT_LINE = ITEMS.registerSimpleBlockItem(ModBlocks.ASPHALT_LINE);
    public static final DeferredItem<BlockItem> SCORCHED_BRICK_STAIRS = ITEMS.registerSimpleBlockItem(ModBlocks.SCORCHED_BRICK_STAIRS);
    public static final DeferredItem<BlockItem> SCORCHED_BRICK_SLAB = ITEMS.registerSimpleBlockItem(ModBlocks.SCORCHED_BRICK_SLAB);
    public static final DeferredItem<BlockItem> SCORCHED_BRICK_WALL = ITEMS.registerSimpleBlockItem(ModBlocks.SCORCHED_BRICK_WALL);
    public static final DeferredItem<BlockItem> CRACKED_SCORCHED_BRICK_STAIRS = ITEMS.registerSimpleBlockItem(ModBlocks.CRACKED_SCORCHED_BRICK_STAIRS);
    public static final DeferredItem<BlockItem> CRACKED_SCORCHED_BRICK_SLAB = ITEMS.registerSimpleBlockItem(ModBlocks.CRACKED_SCORCHED_BRICK_SLAB);
    public static final DeferredItem<BlockItem> CRACKED_SCORCHED_BRICK_WALL = ITEMS.registerSimpleBlockItem(ModBlocks.CRACKED_SCORCHED_BRICK_WALL);
    public static final DeferredItem<BlockItem> SCORCHED_CONCRETE_STAIRS = ITEMS.registerSimpleBlockItem(ModBlocks.SCORCHED_CONCRETE_STAIRS);
    public static final DeferredItem<BlockItem> SCORCHED_CONCRETE_SLAB = ITEMS.registerSimpleBlockItem(ModBlocks.SCORCHED_CONCRETE_SLAB);
    public static final DeferredItem<BlockItem> SCORCHED_CONCRETE_WALL = ITEMS.registerSimpleBlockItem(ModBlocks.SCORCHED_CONCRETE_WALL);
    public static final DeferredItem<BlockItem> CRACKED_SCORCHED_CONCRETE_STAIRS = ITEMS.registerSimpleBlockItem(ModBlocks.CRACKED_SCORCHED_CONCRETE_STAIRS);
    public static final DeferredItem<BlockItem> CRACKED_SCORCHED_CONCRETE_SLAB = ITEMS.registerSimpleBlockItem(ModBlocks.CRACKED_SCORCHED_CONCRETE_SLAB);
    public static final DeferredItem<BlockItem> CRACKED_SCORCHED_CONCRETE_WALL = ITEMS.registerSimpleBlockItem(ModBlocks.CRACKED_SCORCHED_CONCRETE_WALL);
    public static final DeferredItem<BlockItem> CHARRED_STAIRS = ITEMS.registerSimpleBlockItem(ModBlocks.CHARRED_STAIRS);
    public static final DeferredItem<BlockItem> CHARRED_SLAB = ITEMS.registerSimpleBlockItem(ModBlocks.CHARRED_SLAB);
    public static final DeferredItem<BlockItem> CHARRED_FENCE = ITEMS.registerSimpleBlockItem(ModBlocks.CHARRED_FENCE);
    public static final DeferredItem<BlockItem> CHARRED_FENCE_GATE = ITEMS.registerSimpleBlockItem(ModBlocks.CHARRED_FENCE_GATE);
    public static final DeferredItem<BlockItem> CHARRED_DOOR = ITEMS.registerSimpleBlockItem(ModBlocks.CHARRED_DOOR);
    public static final DeferredItem<BlockItem> CHARRED_TRAPDOOR = ITEMS.registerSimpleBlockItem(ModBlocks.CHARRED_TRAPDOOR);

    // materials
    public static final DeferredItem<Item> ASH = ITEMS.registerSimpleItem("ash");
    public static final DeferredItem<Item> SCRAP = ITEMS.registerSimpleItem("scrap");
    public static final DeferredItem<Item> SPECTRAL_ASH = ITEMS.registerItem("spectral_ash", p -> new Item(p) {
        @Override
        public boolean isFoil(ItemStack stack) {
            return true;
        }
    }, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> WARFORGED_PLATING = ITEMS.registerItem("warforged_plating", Item::new,
            new Item.Properties().fireResistant().rarity(Rarity.RARE));
    public static final DeferredItem<Item> WARFORGED_TEMPLATE = ITEMS.register("warforged_upgrade_smithing_template", WarforgedTemplate::create);

    // tools
    public static final DeferredItem<AshIgniterItem> ASH_IGNITER = ITEMS.registerItem("ash_igniter", AshIgniterItem::new,
            new Item.Properties().stacksTo(1).durability(32));
    public static final DeferredItem<EyeOfCratomancerItem> EYE_OF_THE_CRATOMANCER = ITEMS.registerItem("eye_of_the_cratomancer",
            EyeOfCratomancerItem::new, new Item.Properties().rarity(Rarity.RARE));

    // curios
    public static final DeferredItem<DogTagItem> DOG_TAG = ITEMS.registerItem("dog_tag", DogTagItem::new, new Item.Properties());
    public static final DeferredItem<DogTagsItem> DOG_TAGS = ITEMS.registerItem("dog_tags", DogTagsItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<CurioItem> SHRAPNEL_CHARM = ITEMS.registerItem("shrapnel_charm", CurioItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<FieldLanternItem> FIELD_LANTERN = ITEMS.registerItem("field_lantern", FieldLanternItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<RationTinItem> RATION_TIN = ITEMS.registerItem("ration_tin", RationTinItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    // armour
    public static final DeferredItem<ArmorItem> GAS_MASK = ITEMS.register("gas_mask", () -> new ArmorItem(ModArmor.GAS_MASK,
            ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(12))));
    public static final DeferredItem<ArmorItem> WARFORGED_HELMET = armor("warforged_helmet", ArmorItem.Type.HELMET);
    public static final DeferredItem<ArmorItem> WARFORGED_CHESTPLATE = armor("warforged_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final DeferredItem<ArmorItem> WARFORGED_LEGGINGS = armor("warforged_leggings", ArmorItem.Type.LEGGINGS);
    public static final DeferredItem<ArmorItem> WARFORGED_BOOTS = armor("warforged_boots", ArmorItem.Type.BOOTS);
    public static final DeferredItem<SwordItem> WARFORGED_SWORD = ITEMS.register("warforged_sword", () -> new SwordItem(ModArmor.WARFORGED_TIER,
            gear().attributes(SwordItem.createAttributes(ModArmor.WARFORGED_TIER, 3, -2.4f))));
    public static final DeferredItem<PickaxeItem> WARFORGED_PICKAXE = ITEMS.register("warforged_pickaxe", () -> new PickaxeItem(ModArmor.WARFORGED_TIER,
            gear().attributes(DiggerItem.createAttributes(ModArmor.WARFORGED_TIER, 1, -2.8f))));
    public static final DeferredItem<AxeItem> WARFORGED_AXE = ITEMS.register("warforged_axe", () -> new AxeItem(ModArmor.WARFORGED_TIER,
            gear().attributes(DiggerItem.createAttributes(ModArmor.WARFORGED_TIER, 5, -3.0f))));
    public static final DeferredItem<ShovelItem> WARFORGED_SHOVEL = ITEMS.register("warforged_shovel", () -> new ShovelItem(ModArmor.WARFORGED_TIER,
            gear().attributes(DiggerItem.createAttributes(ModArmor.WARFORGED_TIER, 1.5f, -3.0f))));
    public static final DeferredItem<HoeItem> WARFORGED_HOE = ITEMS.register("warforged_hoe", () -> new HoeItem(ModArmor.WARFORGED_TIER,
            gear().attributes(DiggerItem.createAttributes(ModArmor.WARFORGED_TIER, -4f, 0f))));

    // spawn eggs
    public static final DeferredItem<DeferredSpawnEggItem> HOLLOW_SOLDIER_EGG = ITEMS.register("hollow_soldier_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.HOLLOW_SOLDIER, 0x4A4A44, 0x7A6E5A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> ASH_WRAITH_EGG = ITEMS.register("ash_wraith_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.ASH_WRAITH, 0xB8B8B4, 0x5E5E5C, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> CINDER_CROW_EGG = ITEMS.register("cinder_crow_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.CINDER_CROW, 0x1C1C1E, 0xC2551E, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> CRATOMANCER_EGG = ITEMS.register("cratomancer_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.CRATOMANCER, 0x2B2B2B, 0xE0782A, new Item.Properties()));

    private static Item.Properties gear() {
        return new Item.Properties().fireResistant().rarity(Rarity.EPIC);
    }

    private static DeferredItem<ArmorItem> armor(String name, ArmorItem.Type type) {
        return ITEMS.register(name, () -> new ArmorItem(ModArmor.WARFORGED, type,
                gear().durability(type.getDurability(37))));
    }

    public static Component tip(String key) {
        return Component.translatable("tooltip.affermath." + key).withStyle(ChatFormatting.GRAY);
    }

    private ModItems() {}
}
