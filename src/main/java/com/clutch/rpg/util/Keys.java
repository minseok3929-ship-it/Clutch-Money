package com.clutch.rpg.util;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public final class Keys {
    public final NamespacedKey itemId;
    public final NamespacedKey itemTier;
    public final NamespacedKey rarity;
    public final NamespacedKey weaponType;
    public final NamespacedKey requiredStats;
    public final NamespacedKey randomOptions;
    public final NamespacedKey mobId;
    public final NamespacedKey bossId;

    public Keys(Plugin plugin) {
        itemId = new NamespacedKey(plugin, "item_id");
        itemTier = new NamespacedKey(plugin, "item_tier");
        rarity = new NamespacedKey(plugin, "rarity");
        weaponType = new NamespacedKey(plugin, "weapon_type");
        requiredStats = new NamespacedKey(plugin, "required_stats");
        randomOptions = new NamespacedKey(plugin, "random_options");
        mobId = new NamespacedKey(plugin, "mob_id");
        bossId = new NamespacedKey(plugin, "boss_id");
    }
}
