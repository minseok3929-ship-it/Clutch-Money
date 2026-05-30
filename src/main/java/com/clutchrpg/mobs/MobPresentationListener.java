package com.clutchrpg.mobs;

import com.clutchrpg.util.ParticleEffects;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public final class MobPresentationListener implements Listener {
    private final org.bukkit.plugin.Plugin plugin;
    private final MobManager mobManager;

    public MobPresentationListener(org.bukkit.plugin.Plugin plugin, MobManager mobManager) { this.plugin = plugin; this.mobManager = mobManager; }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof org.bukkit.entity.LivingEntity entity)) return;
        if (mobManager.typeOf(entity) == null) return;
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (entity.isValid() && !entity.isDead()) mobManager.updateName(entity);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(EntityDeathEvent event) {
        ForestMobType type = mobManager.typeOf(event.getEntity());
        if (type == null) return;
        switch (type) {
            case FOREST_SLIME -> event.getEntity().getWorld().spawnParticle(Particle.DUST, event.getEntity().getLocation().add(0, 0.6, 0), 45, 0.55, 0.45, 0.55, 0, ParticleEffects.GREEN_DUST);
            case FOREST_WOLF -> event.getEntity().getWorld().spawnParticle(Particle.CLOUD, event.getEntity().getLocation().add(0, 0.5, 0), 30, 0.7, 0.35, 0.7, 0.06);
            case GOBLIN -> event.getEntity().getWorld().spawnParticle(Particle.CRIT, event.getEntity().getLocation().add(0, 0.8, 0), 34, 0.55, 0.45, 0.55, 0.05);
            case VINE_GOLEM -> event.getEntity().getWorld().spawnParticle(Particle.DUST_PLUME, event.getEntity().getLocation(), 70, 0.9, 0.25, 0.9, 0.08);
        }
        event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.BLOCK_GRASS_BREAK, 0.8f, 0.7f);
    }
}
