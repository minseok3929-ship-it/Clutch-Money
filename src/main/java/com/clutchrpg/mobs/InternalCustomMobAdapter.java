package com.clutchrpg.mobs;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public final class InternalCustomMobAdapter implements CustomMobSpawnAdapter {
    @Override public String id() { return "internal"; }
    @Override public boolean isAvailable() { return true; }

    @Override public LivingEntity spawn(CustomMobDefinition definition, Location location) {
        return (LivingEntity) location.getWorld().spawnEntity(location, definition.entityType());
    }
}
