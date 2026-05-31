package com.clutchrpg.mobs;

import com.clutchrpg.util.ParticleEffects;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public final class MobProjectileListener implements Listener {
    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (!event.getEntity().getScoreboardTags().contains("clutchrpg_goblin_projectile")) return;
        var loc = event.getEntity().getLocation();
        loc.getWorld().spawnParticle(Particle.DUST, loc, 24, 0.25, 0.25, 0.25, 0, ParticleEffects.GREEN_DUST);
        loc.getWorld().playSound(loc, Sound.BLOCK_GRASS_BREAK, 0.65f, 1.4f);
        if (event.getHitEntity() instanceof Player player && event.getEntity().getShooter() instanceof org.bukkit.entity.Entity shooter) {
            player.damage(7.0, shooter);
        }
        event.getEntity().remove();
    }
}
