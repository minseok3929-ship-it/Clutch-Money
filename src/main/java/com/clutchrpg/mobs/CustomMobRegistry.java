package com.clutchrpg.mobs;

import java.util.EnumMap;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;

public final class CustomMobRegistry {
    private final FileConfiguration config;
    private final Map<ForestMobType, CustomMobDefinition> definitions = new EnumMap<>(ForestMobType.class);

    public CustomMobRegistry(FileConfiguration config) {
        this.config = config;
        reload();
    }

    public void reload() {
        definitions.clear();
        register(ForestMobType.FOREST_SLIME, config.getDouble("forest.mobs.forest_slime.health", 72.0), 0.36, 0.05, 10, 10.0, 15, 3, 5);
        register(ForestMobType.FOREST_WOLF, config.getDouble("forest.mobs.forest_wolf.health", ForestMobType.FOREST_WOLF.health()), 0.40, 0.15, 5, 9.0, 18, 1, 2);
        register(ForestMobType.GOBLIN, config.getDouble("forest.mobs.goblin.health", ForestMobType.GOBLIN.health()), 0.31, 0.10, 6, 8.0, 20, 2, 4);
        register(ForestMobType.VINE_GOLEM, config.getDouble("forest.mobs.vine_golem.health", ForestMobType.VINE_GOLEM.health()), 0.18, 0.86, 3, 7.0, 24, 1, 1);
    }

    private void register(ForestMobType type, double health, double speed, double knockbackResistance, int maxAlive, double radius, int respawn, int batchMin, int batchMax) {
        String mobPath = "resource-pack.mobs." + type.id();
        String mythicMobId = config.getString(mobPath + ".mythicMobId", type.id());
        String modelEngineId = config.getString(mobPath + ".modelEngineId", type.id());
        definitions.put(type, new CustomMobDefinition(type, type.id(), type.koreanName(), type.entityType(), type.level(), health, type.exp(), speed, knockbackResistance, maxAlive, radius, respawn, batchMin, batchMax, mythicMobId, modelEngineId));
    }

    public CustomMobDefinition definition(ForestMobType type) {
        return definitions.get(type);
    }

    public CustomMobDefinition byId(String id) {
        ForestMobType type = ForestMobType.byId(id);
        return type == null ? null : definition(type);
    }
}
