package com.clutchrpg.combat;

import com.clutchrpg.util.Chat;
import com.clutchrpg.util.CooldownTracker;
import java.time.Duration;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public final class PotionListener implements Listener {
    private final CooldownTracker cooldowns;

    public PotionListener(CooldownTracker cooldowns) { this.cooldowns = cooldowns; }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item.getType() != Material.POTION) return;
        Player player = event.getPlayer();
        if (!cooldowns.isReady(player.getUniqueId(), "potion")) {
            event.setCancelled(true);
            Chat.send(player, "회복 포션 재사용 대기: " + Math.max(1, cooldowns.remainingMillis(player.getUniqueId(), "potion") / 1000) + "초");
            return;
        }
        cooldowns.set(player.getUniqueId(), "potion", Duration.ofSeconds(15));
        double heal = Math.min(player.getHealth() + 35.0, player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue());
        player.setHealth(heal);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 8, 0.35, 0.35, 0.35, 0.03);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 0.7f, 1.2f);
        Chat.send(player, "하급 회복 포션으로 체력 35 회복!");
    }
}
