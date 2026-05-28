package com.clutch.rpg.player;

import com.clutch.rpg.stats.StatType;
import com.clutch.rpg.storage.PlayerStorage;
import com.clutch.rpg.util.MessageUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {
    private final PlayerStorage storage;
    private final Map<UUID, PlayerData> data = new ConcurrentHashMap<>();

    public PlayerDataManager(PlayerStorage storage) {
        this.storage = storage;
    }

    public PlayerData get(Player player) {
        return data.computeIfAbsent(player.getUniqueId(), PlayerData::new);
    }

    public void load(Player player) {
        PlayerData loaded = storage.load(player.getUniqueId()).orElseGet(() -> new PlayerData(player.getUniqueId()));
        data.put(player.getUniqueId(), loaded);
        applyDerivedAttributes(player);
    }

    public void save(Player player) {
        PlayerData playerData = data.get(player.getUniqueId());
        if (playerData != null) {
            storage.save(playerData);
        }
    }

    public void saveAll() {
        data.values().forEach(storage::save);
    }

    public void addExp(Player player, long amount) {
        PlayerData playerData = get(player);
        playerData.setExp(playerData.exp() + amount);
        boolean leveled = false;
        while (playerData.exp() >= expToNext(playerData.level())) {
            playerData.setExp(playerData.exp() - expToNext(playerData.level()));
            playerData.setLevel(playerData.level() + 1);
            playerData.addStatPoints(5);
            leveled = true;
        }
        if (leveled) {
            applyDerivedAttributes(player);
            MessageUtil.send(player, "&b레벨 업! &7현재 레벨: &d" + playerData.level() + " &7/ 스텟 포인트 +5");
        }
    }

    public long expToNext(int level) {
        return 100L + (long) level * level * 35L;
    }

    public boolean addStat(Player player, StatType type, int amount) {
        PlayerData playerData = get(player);
        if (amount <= 0 || playerData.statPoints() < amount) {
            return false;
        }
        playerData.setStatPoints(playerData.statPoints() - amount);
        playerData.stats().add(type, amount);
        applyDerivedAttributes(player);
        return true;
    }

    public void applyDerivedAttributes(Player player) {
        PlayerData playerData = get(player);
        double maxHealth = 20.0 + playerData.stats().scaled(StatType.VIT) * 0.35;
        if (player.getAttribute(Attribute.MAX_HEALTH) != null) {
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
            player.setHealth(Math.min(player.getHealth(), maxHealth));
        }
    }
}
