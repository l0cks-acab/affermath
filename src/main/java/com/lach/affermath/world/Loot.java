package com.lach.affermath.world;

import com.lach.affermath.Affermath;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public final class Loot {
    public static final ResourceKey<LootTable> RUIN = key("chests/ruin");
    public static final ResourceKey<LootTable> BUNKER = key("chests/bunker");
    public static final ResourceKey<LootTable> VAULT = key("chests/bunker_vault");

    private static ResourceKey<LootTable> key(String p) {
        return ResourceKey.create(Registries.LOOT_TABLE, Affermath.id(p));
    }

    private Loot() {}
}
