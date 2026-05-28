package com.clutchrpg.player;

import com.clutchrpg.stats.StatCalculator;
import com.clutchrpg.storage.SQLiteStorage;
import com.clutchrpg.util.Chat;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public final class PlayerManager {
    private final SQLiteStorage storage;
    private final Map<UUID, PlayerProfile> profiles = new ConcurrentHashMap<>();

    public PlayerManager(SQLiteStorage storage) { this.storage = storage; }

    public PlayerProfile get(Player player) {
        return profiles.computeIfAbsent(player.getUniqueId(), PlayerProfile::new);
    }

    public void load(Player player) {
        PlayerProfile profile = storage.load(player.getUniqueId());
        profiles.put(player.getUniqueId(), profile);
        applyDerivedStats(player);
    }

    public void save(Player player) {
        PlayerProfile profile = profiles.get(player.getUniqueId());
        if (profile != null) storage.save(profile);
    }

    public void saveAll(Collection<? extends Player> players) {
        players.forEach(this::save);
    }

    public void applyDerivedStats(Player player) {
        PlayerProfile profile = get(player);
        double maxHealth = StatCalculator.maxHealth(profile);
        if (player.getAttribute(Attribute.MAX_HEALTH) != null) {
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
        }
        player.setWalkSpeed((float) Math.min(0.35, 0.2 * (1.0 + StatCalculator.moveSpeedBonus(profile))));
        if (player.getHealth() > maxHealth) player.setHealth(maxHealth);
    }

    public void addExp(Player player, long amount) {
        PlayerProfile profile = get(player);
        if (profile.level() >= PlayerProfile.MAX_LEVEL) return;
        profile.setExp(profile.exp() + amount);
        boolean leveled = false;
        while (profile.level() < PlayerProfile.MAX_LEVEL && profile.exp() >= StatCalculator.expNeeded(profile.level())) {
            profile.setExp(profile.exp() - StatCalculator.expNeeded(profile.level()));
            profile.setLevel(profile.level() + 1);
            profile.setStatPoints(profile.statPoints() + 5);
            leveled = true;
        }
        if (leveled) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.9f, 1.35f);
            Chat.send(player, "레벨 업! 현재 Lv." + profile.level() + " / 스텟 포인트 +5");
            applyDerivedStats(player);
        }
    }
}
