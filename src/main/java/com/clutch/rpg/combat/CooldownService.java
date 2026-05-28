package com.clutch.rpg.combat;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownService {
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();

    public boolean ready(UUID uuid, String key) {
        return remainingMillis(uuid, key) <= 0;
    }

    public void start(UUID uuid, String key, Duration duration) {
        cooldowns.put(uuid + ":" + key, System.currentTimeMillis() + duration.toMillis());
    }

    public long remainingMillis(UUID uuid, String key) {
        return cooldowns.getOrDefault(uuid + ":" + key, 0L) - System.currentTimeMillis();
    }
}
