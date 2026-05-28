package com.clutchrpg.combat;

import com.clutchrpg.util.Chat;
import com.clutchrpg.util.CooldownTracker;
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
        cooldowns.set(player.getUniqueId(), "dash", Duration.ofSeconds(4));
        Vector dir = player.getLocation().getDirection().normalize().multiply(1.55).setY(0.18);
        player.setVelocity(dir);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 28, 0.35, 0.25, 0.35, 0.06);
        player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation(), 18, 0.45, 0.25, 0.45, 0.04);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.45f, 1.7f);
    }
}
