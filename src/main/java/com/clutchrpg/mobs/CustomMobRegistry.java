package com.clutchrpg.mobs;

import java.util.EnumMap;
import java.util.Map;

public final class CustomMobRegistry {
    private final Map<ForestMobType, CustomMobDefinition> definitions = new EnumMap<>(ForestMobType.class);

    public CustomMobRegistry() {
        register(ForestMobType.FOREST_SLIME, 0.36, 0.05, 10, 10.0, 15, 3, 5);
        register(ForestMobType.FOREST_WOLF, 0.40, 0.15, 5, 9.0, 18, 1, 2);
        register(ForestMobType.GOBLIN, 0.31, 0.10, 6, 8.0, 20, 2, 4);
        register(ForestMobType.VINE_GOLEM, 0.18, 0.86, 3, 7.0, 24, 1, 1);
    }

    private void register(ForestMobType type, double speed, double knockbackResistance, int maxAlive, double radius, int respawn, int batchMin, int batchMax) {
        definitions.put(type, new CustomMobDefinition(type, type.id(), type.koreanName(), type.entityType(), type.level(), type.health(), type.exp(), speed, knockbackResistance, maxAlive, radius, respawn, batchMin, batchMax));
    }

    public CustomMobDefinition definition(ForestMobType type) {
        return definitions.get(type);
    }

    public CustomMobDefinition byId(String id) {
        ForestMobType type = ForestMobType.byId(id);
        return type == null ? null : definition(type);
    }
}
