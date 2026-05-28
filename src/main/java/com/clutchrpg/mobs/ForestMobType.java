package com.clutchrpg.mobs;

import org.bukkit.entity.EntityType;

public enum ForestMobType {
    FOREST_SLIME("forest_slime", "숲 슬라임", EntityType.SLIME, 1, 34, 18),
    FOREST_WOLF("forest_wolf", "숲 늑대", EntityType.WOLF, 3, 48, 28),
    GOBLIN("goblin", "고블린", EntityType.ZOMBIE, 5, 68, 42),
    VINE_GOLEM("vine_golem", "덩굴 골렘", EntityType.IRON_GOLEM, 10, 160, 95);

    private final String id;
    private final String koreanName;
    private final EntityType entityType;
    private final int level;
    private final double health;
    private final int exp;

    ForestMobType(String id, String koreanName, EntityType entityType, int level, double health, int exp) {
        this.id = id; this.koreanName = koreanName; this.entityType = entityType; this.level = level; this.health = health; this.exp = exp;
    }
    public String id() { return id; }
    public String koreanName() { return koreanName; }
    public EntityType entityType() { return entityType; }
    public int level() { return level; }
    public double health() { return health; }
    public int exp() { return exp; }
    public static ForestMobType byId(String id) {
        for (ForestMobType type : values()) if (type.id.equalsIgnoreCase(id)) return type;
        return null;
    }
}
