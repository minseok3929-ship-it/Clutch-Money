package com.clutch.rpg.player;

import com.clutch.rpg.stats.StatBlock;

import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private int level;
    private long exp;
    private int statPoints;
    private final StatBlock stats;

    public PlayerData(UUID uuid) {
        this(uuid, 1, 0, 0, new StatBlock());
    }

    public PlayerData(UUID uuid, int level, long exp, int statPoints, StatBlock stats) {
        this.uuid = uuid;
        this.level = Math.max(1, level);
        this.exp = Math.max(0, exp);
        this.statPoints = Math.max(0, statPoints);
        this.stats = stats;
    }

    public UUID uuid() { return uuid; }
    public int level() { return level; }
    public long exp() { return exp; }
    public int statPoints() { return statPoints; }
    public StatBlock stats() { return stats; }

    public void setLevel(int level) { this.level = Math.max(1, level); }
    public void setExp(long exp) { this.exp = Math.max(0, exp); }
    public void setStatPoints(int statPoints) { this.statPoints = Math.max(0, statPoints); }
    public void addStatPoints(int amount) { setStatPoints(statPoints + amount); }
}
