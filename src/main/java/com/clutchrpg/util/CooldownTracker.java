package com.clutchrpg.util;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CooldownTracker {
    private final Map<String, Long> cooldowns = new HashMap<>();

    public boolean isReady(UUID uuid, String key) {
        return remainingMillis(uuid, key) <= 0;
    }

    public void set(UUID uuid, String key, Duration duration) {
        cooldowns.put(uuid + ":" + key, System.currentTimeMillis() + duration.toMillis());
    }

    public long remainingMillis(UUID uuid, String key) {
        return cooldowns.getOrDefault(uuid + ":" + key, 0L) - System.currentTimeMillis();
    }
}
