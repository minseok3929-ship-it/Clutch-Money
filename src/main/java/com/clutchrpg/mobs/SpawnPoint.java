package com.clutchrpg.mobs;

import org.bukkit.Location;

public record SpawnPoint(
        int id,
        ForestMobType mobType,
        Location location,
        int maxAlive,
        double radius,
        int respawnSeconds,
        int batchMin,
        int batchMax
) {
    public int safeBatchMin() { return Math.max(1, batchMin); }
    public int safeBatchMax() { return Math.max(safeBatchMin(), batchMax); }
    public int safeMaxAlive() { return Math.max(1, maxAlive); }
    public double safeRadius() { return Math.max(1.0, radius); }
    public int safeRespawnSeconds() { return Math.max(3, respawnSeconds); }
}
