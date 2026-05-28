package com.clutchrpg.items;

import com.clutchrpg.player.PlayerManager;
import com.clutchrpg.stats.StatCalculator;
import com.clutchrpg.util.Chat;
import java.util.Map;
import java.util.Random;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class DropService {
    private final PlayerManager playerManager;
    private final ItemFactory itemFactory;
    private final Random random = new Random();

    public DropService(PlayerManager playerManager, ItemFactory itemFactory) {
        this.playerManager = playerManager; this.itemFactory = itemFactory;
    }

    public void rollForestDrop(Player player, double rareBias) {
        double dropChance = 0.28 + StatCalculator.dropRateBonus(playerManager.get(player));
        if (random.nextDouble() > dropChance) return;
        Rarity rarity = rollRarity(player, rareBias);
        WeaponType[] weapons = {WeaponType.SWORD, WeaponType.BOW, WeaponType.STAFF};
        ItemStack item = itemFactory.createWeapon(weapons[random.nextInt(weapons.length)], rarity);
        give(player, item);
        if (rarity.ordinal() >= Rarity.RARE.ordinal()) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.9f, rarity == Rarity.EPIC ? 1.25f : 1.0f);
            player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 45, 0.55, 0.7, 0.55, 0.08);
            Chat.send(player, rarity.korean() + " 장비 획득! 인벤토리를 확인하세요.");
        }
    }

    private Rarity rollRarity(Player player, double rareBias) {
        double rareBonus = StatCalculator.rareDropBonus(playerManager.get(player)) + rareBias;
        double r = random.nextDouble();
        if (r < 0.04 + rareBonus) return Rarity.EPIC;
        if (r < 0.18 + rareBonus * 2) return Rarity.RARE;
        return Rarity.COMMON;
    }

    public void give(Player player, ItemStack item) {
        Map<Integer, ItemStack> left = player.getInventory().addItem(item);
        left.values().forEach(stack -> player.getWorld().dropItemNaturally(player.getLocation(), stack));
    }
}
