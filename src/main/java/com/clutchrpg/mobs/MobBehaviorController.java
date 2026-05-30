package com.clutchrpg.mobs;

import com.clutchrpg.util.ParticleEffects;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public final class MobBehaviorController {
    private final Plugin plugin;
    private final MobManager mobManager;
    private BukkitTask task;

    public MobBehaviorController(Plugin plugin, MobManager mobManager) {
        this.plugin = plugin;
        this.mobManager = mobManager;
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 35L, 35L);
    }

    public void stop() {
        if (task != null) task.cancel();
    }

    private void tick() {
        for (var world : Bukkit.getWorlds()) {
            for (LivingEntity mob : world.getLivingEntities()) {
                ForestMobType type = mobManager.typeOf(mob);
                if (type == null || mob.isDead()) continue;
                Player target = nearestPlayer(mob, 14.0);
                if (target == null) continue;
                switch (type) {
                    case FOREST_SLIME -> slimeLeap(mob, target);
                    case FOREST_WOLF -> wolfCharge(mob, target);
                    case GOBLIN -> goblinTactics(mob, target);
                    case VINE_GOLEM -> golemSlam(mob);
                }
            }
        }
    }

    private Player nearestPlayer(LivingEntity mob, double range) {
        return mob.getWorld().getNearbyEntities(mob.getLocation(), range, range, range).stream()
                .filter(e -> e instanceof Player)
                .map(e -> (Player) e)
                .min(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(mob.getLocation())))
                .orElse(null);
    }

    private void slimeLeap(LivingEntity mob, Player target) {
        if (ThreadLocalRandom.current().nextDouble() > 0.55) return;
        Vector leap = target.getLocation().toVector().subtract(mob.getLocation().toVector()).normalize().multiply(0.85).setY(0.48);
        mob.setVelocity(leap);
        mob.getWorld().spawnParticle(Particle.DUST, mob.getLocation().add(0, 0.4, 0), 20, 0.35, 0.2, 0.35, 0, ParticleEffects.GREEN_DUST);
        mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_SLIME_JUMP, 0.9f, 1.35f);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!mob.isValid() || mob.isDead()) return;
            mob.getWorld().spawnParticle(Particle.DUST, mob.getLocation().add(0, 0.15, 0), 28, 0.55, 0.15, 0.55, 0, ParticleEffects.DARK_GREEN_DUST);
            mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_SLIME_SQUISH, 0.7f, 1.4f);
        }, 14L);
    }

    private void wolfCharge(LivingEntity mob, Player target) {
        if (ThreadLocalRandom.current().nextDouble() > 0.38) return;
        Location warning = mob.getLocation().add(0, 0.55, 0);
        mob.getWorld().spawnParticle(Particle.DUST, warning, 32, 0.45, 0.25, 0.45, 0, ParticleEffects.RED_DUST);
        mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_WOLF_GROWL, 0.75f, 1.45f);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!mob.isValid() || mob.isDead() || !target.isOnline()) return;
            Vector charge = target.getLocation().toVector().subtract(mob.getLocation().toVector()).normalize().multiply(1.32).setY(0.12);
            mob.setVelocity(charge);
            mob.getWorld().spawnParticle(Particle.CLOUD, mob.getLocation(), 26, 0.35, 0.2, 0.35, 0.08);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (mob.isValid() && !mob.isDead()) mob.setVelocity(mob.getVelocity().multiply(0.25));
            }, 12L);
        }, 10L);
    }

    private void goblinTactics(LivingEntity mob, Player target) {
        rallyNearbyGoblins(mob, target);
        double healthRatio = mob.getHealth() / mob.getAttribute(Attribute.MAX_HEALTH).getValue();
        double distanceSquared = mob.getLocation().distanceSquared(target.getLocation());
        if (healthRatio < 0.35 || distanceSquared < 18.0) {
            Vector away = mob.getLocation().toVector().subtract(target.getLocation().toVector()).normalize().multiply(0.55).setY(0.08);
            mob.setVelocity(mob.getVelocity().add(away));
            mob.getWorld().spawnParticle(Particle.CLOUD, mob.getLocation().add(0, 0.4, 0), 12, 0.25, 0.12, 0.25, 0.04);
        }
        if (ThreadLocalRandom.current().nextDouble() > 0.42) return;
        mob.getWorld().spawnParticle(Particle.DUST, mob.getLocation().add(0, 1.2, 0), 18, 0.3, 0.3, 0.3, 0, ParticleEffects.WOOD_DUST);
        Snowball shot = mob.launchProjectile(Snowball.class);
        shot.setVelocity(target.getEyeLocation().toVector().subtract(mob.getEyeLocation().toVector()).normalize().multiply(1.45));
        shot.addScoreboardTag("clutchrpg_goblin_projectile");
        mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 0.7f, 0.85f);
    }

    private void rallyNearbyGoblins(LivingEntity mob, Player target) {
        mob.getWorld().getNearbyEntities(mob.getLocation(), 8, 3, 8).stream()
                .filter(e -> e instanceof LivingEntity ally && ally != mob && mobManager.typeOf(ally) == ForestMobType.GOBLIN)
                .map(e -> (LivingEntity) e)
                .limit(3)
                .forEach(ally -> {
                    Vector flank = target.getLocation().toVector().subtract(ally.getLocation().toVector()).normalize().multiply(0.25).setY(0.02);
                    ally.setVelocity(ally.getVelocity().add(flank));
                    ally.getWorld().spawnParticle(Particle.DUST, ally.getLocation().add(0, 1.0, 0), 5, 0.18, 0.18, 0.18, 0, ParticleEffects.DARK_GREEN_DUST);
                });
    }

    private void golemSlam(LivingEntity mob) {
        if (ThreadLocalRandom.current().nextDouble() > 0.32) return;
        Location center = mob.getLocation();
        ParticleEffects.ring(center, 3.5, Particle.DUST, ParticleEffects.GREEN_DUST, 54);
        mob.getWorld().spawnParticle(Particle.DUST_PLUME, center, 18, 1.0, 0.08, 1.0, 0.03);
        mob.getWorld().playSound(center, Sound.BLOCK_ROOTED_DIRT_BREAK, 0.9f, 0.6f);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!mob.isValid() || mob.isDead()) return;
            mob.getWorld().spawnParticle(Particle.EXPLOSION, center.add(0, 0.1, 0), 1, 0.1, 0.1, 0.1, 0);
            mob.getWorld().spawnParticle(Particle.DUST_PLUME, center, 75, 2.6, 0.25, 2.6, 0.08);
            ParticleEffects.expandingRing(center, 3.5, ParticleEffects.WOOD_DUST, 4, 1L, plugin);
            mob.getWorld().playSound(center, Sound.ENTITY_IRON_GOLEM_ATTACK, 1.0f, 0.7f);
            mob.getWorld().getNearbyEntities(center, 3.5, 2.0, 3.5).stream()
                    .filter(e -> e instanceof Player)
                    .map(e -> (Player) e)
                    .forEach(player -> {
                        player.damage(11.0, mob);
                        player.setVelocity(player.getLocation().toVector().subtract(center.toVector()).normalize().multiply(0.55).setY(0.28));
                    });
        }, 24L);
    }
}
