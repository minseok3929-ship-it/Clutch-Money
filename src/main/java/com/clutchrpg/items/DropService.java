package com.clutchrpg.items;

import com.clutchrpg.player.PlayerManager;
import com.clutchrpg.stats.StatCalculator;
import com.clutchrpg.util.Chat;
import com.clutchrpg.util.ParticleEffects;
import java.util.Map;
import java.util.Random;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
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
        presentDrop(player, rarity);
    }

    private Rarity rollRarity(Player player, double rareBias) {
        double rareBonus = StatCalculator.rareDropBonus(playerManager.get(player)) + rareBias;
        double r = random.nextDouble();
        if (r < 0.04 + rareBonus) return Rarity.EPIC;
        if (r < 0.18 + rareBonus * 2) return Rarity.RARE;
        return Rarity.COMMON;
    }

    private void presentDrop(Player player, Rarity rarity) {
        if (rarity == Rarity.COMMON) return;
        if (rarity == Rarity.RARE) {
            player.sendActionBar(Chat.PREFIX.append(Component.text("희귀 장비를 획득했습니다!", rarity.color())));
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.9f, 1.7f);
            player.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(0, 1, 0), 70, 0.65, 0.8, 0.65, 0, ParticleEffects.CYAN_DUST);
            player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0, 1, 0), 24, 0.55, 0.55, 0.55, 0.05);
            Chat.send(player, "희귀 장비를 획득했습니다!");
            return;
        }
        player.showTitle(Title.title(Component.text("영웅 장비 획득!", rarity.color()), Component.text("인벤토리를 확인하세요", Chat.CYAN)));
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.05f);
        player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 0.9f, 1.25f);
        player.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(0, 1, 0), 90, 0.8, 0.9, 0.8, 0, ParticleEffects.PURPLE_DUST);
        player.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(0, 1, 0), 70, 0.8, 0.9, 0.8, 0, ParticleEffects.CYAN_DUST);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 50, 0.7, 0.7, 0.7, 0.08);
        Chat.send(player, "영웅 장비를 획득했습니다!");
    }

    public void give(Player player, ItemStack item) {
        Map<Integer, ItemStack> left = player.getInventory().addItem(item);
        left.values().forEach(stack -> player.getWorld().dropItemNaturally(player.getLocation(), stack));
    }
}
