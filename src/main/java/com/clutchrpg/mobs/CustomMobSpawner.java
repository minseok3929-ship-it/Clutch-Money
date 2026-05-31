package com.clutchrpg.mobs;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public final class CustomMobSpawner {
    private final List<CustomMobSpawnAdapter> adapters;

    public CustomMobSpawner(List<CustomMobSpawnAdapter> adapters) {
        this.adapters = List.copyOf(adapters);
    }

    public LivingEntity spawn(CustomMobDefinition definition, Location location) {
        for (CustomMobSpawnAdapter adapter : adapters) {
            if (!adapter.isAvailable()) continue;
            LivingEntity spawned = adapter.spawn(definition, location);
            if (spawned != null) return spawned;
        }
        throw new IllegalStateException("No custom mob adapter could spawn mobId=" + definition.id());
    }
}
