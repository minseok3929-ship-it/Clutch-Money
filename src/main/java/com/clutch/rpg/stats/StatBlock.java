package com.clutch.rpg.stats;

import java.util.EnumMap;
import java.util.Map;

public class StatBlock {
    private final EnumMap<StatType, Integer> values = new EnumMap<>(StatType.class);

    public StatBlock() {
        for (StatType type : StatType.values()) {
            values.put(type, 0);
        }
    }

    public int get(StatType type) {
        return values.getOrDefault(type, 0);
    }

    public void set(StatType type, int value) {
        values.put(type, Math.max(0, value));
    }

    public void add(StatType type, int amount) {
        set(type, get(type) + amount);
    }

    public Map<StatType, Integer> asMap() {
        return Map.copyOf(values);
    }

    public double scaled(StatType type) {
        int raw = get(type);
        if (raw <= 50) {
            return raw;
        }
        if (raw <= 100) {
            return 50 + (raw - 50) * 0.6;
        }
        return 80 + (raw - 100) * 0.3;
    }
}
