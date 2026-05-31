package com.clutchrpg.mobs;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public interface CustomMobSpawnAdapter {
    String id();
    boolean isAvailable();
    LivingEntity spawn(CustomMobDefinition definition, Location location);
}
