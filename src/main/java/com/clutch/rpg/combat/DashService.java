package com.clutch.rpg.combat;

import com.clutch.rpg.items.CustomItemFactory;
import com.clutch.rpg.items.OptionType;
import com.clutch.rpg.util.MessageUtil;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;

public class DashService {
    private final Plugin plugin;
    private final CooldownService cooldowns;
    private final CustomItemFactory itemFactory;

    public DashService(Plugin plugin, CooldownService cooldowns, CustomItemFactory itemFactory) {
        this.plugin = plugin;
        this.cooldowns = cooldowns;
        this.itemFactory = itemFactory;
    }

    public void tryDash(Player player) {
        if (player.isOnGround()) {
            return;
        }
        String key = "dash";
        if (!cooldowns.ready(player.getUniqueId(), key)) {
            MessageUtil.send(player, "&7대쉬 쿨타임: &b" + String.format("%.1f", cooldowns.remainingMillis(player.getUniqueId(), key) / 1000.0) + "초");
            return;
        }
        player.setVelocity(player.getLocation().getDirection().normalize().multiply(1.45).setY(0.18));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_JUMP, 0.8f, 1.35f);
        new BukkitRunnable() {
            private int ticks;
            @Override
            public void run() {
                if (ticks++ >= 8 || !player.isOnline()) {
                    cancel();
                    return;
                }
                player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, player.getLocation().add(0, 0.6, 0), 8, 0.25, 0.25, 0.25, 0.01);
            }
        }.runTaskTimer(plugin, 0L, 1L);
        double reduction = itemFactory.optionSum(player.getInventory().getItemInMainHand(), OptionType.DASH_COOLDOWN_REDUCTION);
        long millis = Math.max(1000L, Math.round(4500.0 * (1.0 - Math.min(70.0, reduction) / 100.0)));
        cooldowns.start(player.getUniqueId(), key, Duration.ofMillis(millis));
    }
}
