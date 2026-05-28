package com.clutch.rpg.mobs;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;

public enum ForestMobType {
    FOREST_SLIME("숲 슬라임", EntityType.SLIME, 1, 35, 18.0),
    FOREST_WOLF("숲 늑대", EntityType.WOLF, 3, 55, 24.0),
    GOBLIN("고블린", EntityType.ZOMBIE, 5, 80, 32.0),
    VINE_GOLEM("덩굴 골렘", EntityType.IRON_GOLEM, 8, 140, 75.0);

    private final String displayName;
    private final EntityType entityType;
    private final int level;
    private final int exp;
    private final double health;

    ForestMobType(String displayName, EntityType entityType, int level, int exp, double health) {
        this.displayName = displayName;
        this.entityType = entityType;
        this.level = level;
        this.exp = exp;
        this.health = health;
    }

    public String displayName() { return ChatColor.DARK_AQUA + displayName + ChatColor.GRAY + " Lv." + level; }
    public EntityType entityType() { return entityType; }
    public int level() { return level; }
    public int exp() { return exp; }
    public double health() { return health; }
}
