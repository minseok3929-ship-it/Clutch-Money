package com.clutch.rpg.items;

import com.clutch.rpg.stats.StatBlock;
import com.clutch.rpg.stats.StatType;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RequiredStats {
    private final EnumMap<StatType, Integer> required = new EnumMap<>(StatType.class);

    public void set(StatType type, int amount) {
        if (amount > 0) {
            required.put(type, amount);
        }
    }

    public boolean meets(StatBlock stats) {
        return required.entrySet().stream().allMatch(entry -> stats.get(entry.getKey()) >= entry.getValue());
    }

    public String serialize() {
        return required.entrySet().stream()
                .map(entry -> entry.getKey().name() + ":" + entry.getValue())
                .collect(Collectors.joining(","));
    }

    public static RequiredStats parse(String raw) {
        RequiredStats stats = new RequiredStats();
        if (raw == null || raw.isBlank()) {
            return stats;
        }
        for (String token : raw.split(",")) {
            String[] split = token.split(":", 2);
            stats.set(StatType.valueOf(split[0]), Integer.parseInt(split[1]));
        }
        return stats;
    }

    public Map<StatType, Integer> asMap() {
        return Map.copyOf(required);
    }
}
