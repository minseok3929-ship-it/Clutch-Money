package com.clutchrpg.combat;

import com.clutchrpg.util.Chat;
import com.clutchrpg.util.CooldownTracker;
import com.clutchrpg.util.ParticleEffects;
import java.time.Duration;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;

public final class DashListener implements Listener {
    private final CooldownTracker cooldowns;

    public DashListener(CooldownTracker cooldowns) { this.cooldowns = cooldowns; }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;
        Player player = event.getPlayer();
        if (player.isOnGround()) return;
        if (!cooldowns.isReady(player.getUniqueId(), "dash")) {
            long left = Math.max(1, cooldowns.remainingMillis(player.getUniqueId(), "dash") / 1000);
            Chat.send(player, "대쉬 재사용 대기: " + left + "초");
            return;
        }
        cooldowns.set(player.getUniqueId(), "dash", Duration.ofSeconds(3));
        Vector direction = player.getLocation().getDirection().normalize();
        Vector velocity = direction.clone().multiply(1.75).setY(0.20);
        player.setVelocity(velocity);
        for (int i = 0; i < 8; i++) {
            player.getWorld().spawnParticle(Particle.DUST, player.getLocation().subtract(direction.clone().multiply(i * 0.28)).add(0, 0.75, 0), 7, 0.18, 0.18, 0.18, 0, i % 2 == 0 ? ParticleEffects.CYAN_DUST : ParticleEffects.PURPLE_DUST);
        }
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 34, 0.35, 0.25, 0.35, 0.08);
        player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(direction.multiply(1.1)), 24, 0.45, 0.25, 0.45, 0.05);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.55f, 1.85f);
        player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, 0.55f, 1.45f);
    }
}
