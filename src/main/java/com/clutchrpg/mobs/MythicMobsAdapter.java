package com.clutchrpg.mobs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public final class MythicMobsAdapter implements CustomMobSpawnAdapter {
    @Override public String id() { return "mythicmobs"; }
    @Override public boolean isAvailable() { return Bukkit.getPluginManager().isPluginEnabled("MythicMobs"); }

    @Override public LivingEntity spawn(CustomMobDefinition definition, Location location) {
        // Integration seam: keep mobId-driven routing here. The MVP falls back to InternalCustomMobAdapter
        // until MythicMobs is added as an optional compile/runtime integration.
        return null;
    }
}
