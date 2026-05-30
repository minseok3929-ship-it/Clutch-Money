package com.clutchrpg.mobs;

import com.clutchrpg.util.ParticleEffects;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 45L, 45L);
    }

    public void stop() {
        if (task != null) task.cancel();
    }

    private void tick() {
        for (var world : Bukkit.getWorlds()) {
            for (LivingEntity mob : world.getLivingEntities()) {
                ForestMobType type = mobManager.typeOf(mob);
                if (type == null || mob.isDead()) continue;
                Player target = nearestPlayer(mob, 12.0);
                if (target == null) continue;
                switch (type) {
                    case FOREST_SLIME -> slimeLeap(mob, target);
                    case FOREST_WOLF -> wolfCharge(mob, target);
                    case GOBLIN -> goblinShot(mob, target);
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
        if (ThreadLocalRandom.current().nextDouble() > 0.45) return;
        Vector leap = target.getLocation().toVector().subtract(mob.getLocation().toVector()).normalize().multiply(0.65).setY(0.45);
        mob.setVelocity(leap);
        mob.getWorld().spawnParticle(Particle.DUST, mob.getLocation().add(0, 0.4, 0), 18, 0.35, 0.2, 0.35, 0, ParticleEffects.GREEN_DUST);
        mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_SLIME_JUMP, 0.8f, 1.2f);
    }

    private void wolfCharge(LivingEntity mob, Player target) {
        if (ThreadLocalRandom.current().nextDouble() > 0.35) return;
        Location warning = mob.getLocation().add(0, 0.55, 0);
        mob.getWorld().spawnParticle(Particle.DUST, warning, 28, 0.45, 0.25, 0.45, 0, ParticleEffects.RED_DUST);
        mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_WOLF_GROWL, 0.7f, 1.35f);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!mob.isValid() || mob.isDead() || !target.isOnline()) return;
            Vector charge = target.getLocation().toVector().subtract(mob.getLocation().toVector()).normalize().multiply(1.15).setY(0.12);
            mob.setVelocity(charge);
            mob.getWorld().spawnParticle(Particle.CLOUD, mob.getLocation(), 22, 0.35, 0.2, 0.35, 0.08);
        }, 12L);
    }

    private void goblinShot(LivingEntity mob, Player target) {
        if (ThreadLocalRandom.current().nextDouble() > 0.32) return;
        mob.getWorld().spawnParticle(Particle.CRIT, mob.getLocation().add(0, 1.2, 0), 18, 0.3, 0.3, 0.3, 0.04);
        Snowball shot = mob.launchProjectile(Snowball.class);
        shot.setVelocity(target.getEyeLocation().toVector().subtract(mob.getEyeLocation().toVector()).normalize().multiply(1.35));
        shot.addScoreboardTag("clutchrpg_goblin_projectile");
        mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 0.65f, 0.75f);
        Vector away = mob.getLocation().toVector().subtract(target.getLocation().toVector()).normalize().multiply(0.28).setY(0.05);
        mob.setVelocity(mob.getVelocity().add(away));
    }

    private void golemSlam(LivingEntity mob) {
        if (ThreadLocalRandom.current().nextDouble() > 0.28) return;
        Location center = mob.getLocation();
        ParticleEffects.ring(center, 3.3, Particle.DUST, ParticleEffects.GREEN_DUST, 48);
        mob.getWorld().playSound(center, Sound.BLOCK_ROOTED_DIRT_BREAK, 0.85f, 0.65f);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!mob.isValid() || mob.isDead()) return;
            mob.getWorld().spawnParticle(Particle.DUST_PLUME, center, 65, 2.6, 0.25, 2.6, 0.08);
            mob.getWorld().playSound(center, Sound.ENTITY_IRON_GOLEM_ATTACK, 0.9f, 0.75f);
            mob.getWorld().getNearbyEntities(center, 3.3, 2.0, 3.3).stream()
                    .filter(e -> e instanceof Player)
                    .map(e -> (Player) e)
                    .forEach(player -> player.damage(10.0, mob));
        }, 24L);
    }
}
