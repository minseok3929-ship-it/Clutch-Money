package com.clutchrpg.mobs;

import org.bukkit.entity.EntityType;

public record CustomMobDefinition(
        ForestMobType type,
        String id,
        String koreanName,
        EntityType entityType,
        int level,
        double maxHealth,
        int exp,
        double movementSpeed,
        double knockbackResistance,
        int defaultMaxAlive,
        double defaultSpawnRadius,
        int defaultRespawnSeconds,
        int defaultBatchMin,
        int defaultBatchMax
) {
}
