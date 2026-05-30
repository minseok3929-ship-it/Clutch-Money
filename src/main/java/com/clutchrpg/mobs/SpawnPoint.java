package com.clutchrpg.mobs;

import org.bukkit.Location;

public record SpawnPoint(int id, ForestMobType mobType, Location location) {
}
