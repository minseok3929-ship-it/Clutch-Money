package com.clutchrpg.player;

import com.clutchrpg.stats.StatType;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerProfile {
    public static final int MAX_LEVEL = 50;
    private final UUID uuid;
    private int level;
    private long exp;
    private int statPoints;
    private final EnumMap<StatType, Integer> stats = new EnumMap<>(StatType.class);

    public PlayerProfile(UUID uuid) {
        this.uuid = uuid;
        this.level = 1;
        this.exp = 0;
        this.statPoints = 0;
        stats.put(StatType.STR, 5);
        stats.put(StatType.DEX, 5);
        stats.put(StatType.INT, 5);
        stats.put(StatType.VIT, 5);
        stats.put(StatType.LUK, 1);
    }

    public UUID uuid() { return uuid; }
    public int level() { return level; }
    public void setLevel(int level) { this.level = Math.max(1, Math.min(MAX_LEVEL, level)); }
    public long exp() { return exp; }
    public void setExp(long exp) { this.exp = Math.max(0, exp); }
    public int statPoints() { return statPoints; }
    public void setStatPoints(int statPoints) { this.statPoints = Math.max(0, statPoints); }
    public int stat(StatType type) { return stats.getOrDefault(type, 0); }
    public Map<StatType, Integer> stats() { return Map.copyOf(stats); }
    public void setStat(StatType type, int value) { stats.put(type, Math.max(0, value)); }

    public boolean addStat(StatType type, int amount) {
        if (amount <= 0 || statPoints < amount) return false;
        stats.put(type, stat(type) + amount);
        statPoints -= amount;
        return true;
    }
}
