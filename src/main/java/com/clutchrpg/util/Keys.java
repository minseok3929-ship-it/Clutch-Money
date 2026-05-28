package com.clutchrpg.util;

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
        this.itemId = new NamespacedKey(plugin, "item_id");
        this.itemTier = new NamespacedKey(plugin, "item_tier");
        this.rarity = new NamespacedKey(plugin, "rarity");
        this.weaponType = new NamespacedKey(plugin, "weapon_type");
        this.requiredStats = new NamespacedKey(plugin, "required_stats");
        this.randomOptions = new NamespacedKey(plugin, "random_options");
        this.mobId = new NamespacedKey(plugin, "mob_id");
        this.bossId = new NamespacedKey(plugin, "boss_id");
    }
}
