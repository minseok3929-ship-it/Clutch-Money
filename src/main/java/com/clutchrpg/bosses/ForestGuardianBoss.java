package com.clutchrpg.bosses;

import com.clutchrpg.mobs.ForestMobType;
import com.clutchrpg.mobs.MobManager;
import com.clutchrpg.util.Chat;
import com.clutchrpg.util.Keys;
import com.clutchrpg.util.ParticleEffects;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public final class ForestGuardianBoss {
    public static final String ID = "forest_guardian";
    private static final double MAX_HEALTH = 950.0;
    private final Plugin plugin;
    private final Keys keys;
    private final MobManager mobManager;
    private BukkitTask patternTask;
    private BukkitTask barTask;
    private BossBar bossBar;
    private UUID activeBossId;

    public ForestGuardianBoss(Plugin plugin, Keys keys, MobManager mobManager) {
        this.plugin = plugin; this.keys = keys; this.mobManager = mobManager;
    }

    public LivingEntity spawn(Location location) {
        cleanupBar();
        LivingEntity boss = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.RAVAGER);
        boss.customName(Component.text("숲의 수호자  ❤ 950/950", NamedTextColor.DARK_GREEN));
        boss.setCustomNameVisible(true);
        boss.getAttribute(Attribute.MAX_HEALTH).setBaseValue(MAX_HEALTH);
        boss.setHealth(MAX_HEALTH);
        boss.getPersistentDataContainer().set(keys.bossId, PersistentDataType.STRING, ID);
        boss.setRemoveWhenFarAway(false);
        activeBossId = boss.getUniqueId();
        bossBar = Bukkit.createBossBar("숲의 수호자", BarColor.GREEN, BarStyle.SEGMENTED_10);
        startBars(boss);
        startPatterns(boss);
        return boss;
    }

    private void startBars(LivingEntity boss) {
        barTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!boss.isValid() || boss.isDead()) {
                cleanupBar();
                return;
            }
            double progress = Math.max(0.0, Math.min(1.0, boss.getHealth() / MAX_HEALTH));
            bossBar.setProgress(progress);
            bossBar.setTitle("숲의 수호자  " + (int) Math.ceil(boss.getHealth()) + "/" + (int) MAX_HEALTH);
            boss.customName(Component.text("숲의 수호자  ❤ " + (int) Math.ceil(boss.getHealth()) + "/" + (int) MAX_HEALTH, NamedTextColor.DARK_GREEN));
            for (Player player : boss.getWorld().getPlayers()) {
                if (player.getLocation().distanceSquared(boss.getLocation()) <= 55 * 55) bossBar.addPlayer(player);
                else bossBar.removePlayer(player);
            }
        }, 0L, 20L);
    }

    private void startPatterns(LivingEntity boss) {
        if (patternTask != null) patternTask.cancel();
        patternTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!boss.isValid() || boss.isDead()) {
                cleanupBar();
                patternTask.cancel();
                return;
            }
            int pattern = ThreadLocalRandom.current().nextInt(4);
            if (pattern == 0) telegraphedSlam(boss);
            if (pattern == 1) vineEruption(boss);
            if (pattern == 2) summon(boss);
            if (pattern == 3) shockwave(boss);
        }, 80L, 120L);
    }

    private void warnNearby(LivingEntity boss, String message) {
        boss.getWorld().getPlayers().stream()
                .filter(player -> player.getLocation().distanceSquared(boss.getLocation()) <= 45 * 45)
                .forEach(player -> player.sendActionBar(Chat.PREFIX.append(Component.text(message, Chat.PURPLE))));
    }

    private void telegraphedSlam(LivingEntity boss) {
        Location center = boss.getLocation().clone();
        warnNearby(boss, "숲의 수호자가 내려찍기를 준비합니다!");
        boss.getWorld().playSound(center, Sound.ENTITY_RAVAGER_ROAR, 1.1f, 0.75f);
        boss.getWorld().spawnParticle(Particle.DUST_PLUME, boss.getLocation().add(0, 0.2, 0), 45, 1.2, 0.12, 1.2, 0.04);
        boss.getWorld().spawnParticle(Particle.BLOCK, boss.getLocation().add(0, 0.8, 0), 30, 0.9, 0.7, 0.9, 0.05, org.bukkit.Material.OAK_LOG.createBlockData());
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!boss.isValid() || boss.isDead()) return;
            boss.setVelocity(new Vector(0, 0.18, 0));
            boss.getWorld().spawnParticle(Particle.DUST, boss.getLocation().add(0, 2.2, 0), 55, 0.7, 0.35, 0.7, 0, ParticleEffects.WOOD_DUST);
            boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_RAVAGER_STEP, 1.0f, 0.55f);
        }, 12L);
        for (int i = 0; i < 6; i++) {
            final int tick = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> ParticleEffects.ring(center, 4.2, Particle.DUST, tick % 2 == 0 ? ParticleEffects.RED_DUST : ParticleEffects.WOOD_DUST, 72), i * 5L);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!boss.isValid() || boss.isDead()) return;
            ParticleEffects.expandingRing(center, 4.2, ParticleEffects.WOOD_DUST, 6, 1L, plugin);
            center.getWorld().spawnParticle(Particle.EXPLOSION, center, 4, 0.35, 0.15, 0.35, 0);
            center.getWorld().spawnParticle(Particle.DUST_PLUME, center, 120, 3.2, 0.3, 3.2, 0.09);
            center.getWorld().spawnParticle(Particle.BLOCK, center, 45, 2.0, 0.25, 2.0, 0.08, org.bukkit.Material.OAK_LOG.createBlockData());
            center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.1f, 0.65f);
            center.getWorld().getNearbyEntities(center, 4.2, 2.5, 4.2).stream()
                    .filter(e -> e instanceof Player)
                    .map(e -> (Player) e)
                    .filter(player -> player.getLocation().distanceSquared(center) <= 4.2 * 4.2)
                    .forEach(player -> {
                        player.damage(28.0, boss);
                        player.setVelocity(player.getLocation().toVector().subtract(center.toVector()).normalize().multiply(1.0).setY(0.45));
                    });
        }, 30L);
    }

    private void vineEruption(LivingEntity boss) {
        warnNearby(boss, "덩굴이 땅속에서 솟아오릅니다!");
        List<Location> zones = new ArrayList<>();
        for (Player player : boss.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(boss.getLocation()) <= 35 * 35) zones.add(player.getLocation().clone());
        }
        while (zones.size() < 4) zones.add(boss.getLocation().clone().add(ThreadLocalRandom.current().nextDouble(-7, 7), 0, ThreadLocalRandom.current().nextDouble(-7, 7)));
        for (Location zone : zones) {
            for (int i = 0; i < 5; i++) {
                final int step = i;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    ParticleEffects.ring(zone, 2.2, Particle.DUST, ParticleEffects.GREEN_DUST, 42);
                    zone.getWorld().spawnParticle(Particle.DUST, zone.clone().add(0, 0.35 + step * 0.08, 0), 14, 0.45, 0.18, 0.45, 0, ParticleEffects.DARK_GREEN_DUST);
                }, i * 6L);
            }
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!boss.isValid() || boss.isDead()) return;
                zone.getWorld().spawnParticle(Particle.DUST_PLUME, zone, 55, 1.6, 0.2, 1.6, 0.08);
                zone.getWorld().spawnParticle(Particle.BLOCK, zone, 35, 1.2, 0.15, 1.2, 0.08, org.bukkit.Material.OAK_LEAVES.createBlockData());
                zone.getWorld().playSound(zone, Sound.BLOCK_ROOTED_DIRT_BREAK, 1.0f, 0.65f);
                zone.getWorld().getNearbyEntities(zone, 2.2, 2.4, 2.2).stream()
                        .filter(e -> e instanceof Player)
                        .map(e -> (Player) e)
                        .filter(player -> player.getLocation().distanceSquared(zone) <= 2.2 * 2.2)
                        .forEach(player -> {
                            player.damage(18.0, boss);
                            player.setVelocity(player.getVelocity().setY(0.45));
                        });
            }, 34L);
        }
    }

    private void summon(LivingEntity boss) {
        warnNearby(boss, "숲의 수호자가 숲의 하수인을 부릅니다!");
        Location loc = boss.getLocation();
        for (int i = 0; i < 4; i++) {
            Location spawn = loc.clone().add(ThreadLocalRandom.current().nextDouble(-5, 5), 0, ThreadLocalRandom.current().nextDouble(-5, 5));
            for (int pulse = 0; pulse < 4; pulse++) {
                final int step = pulse;
                Bukkit.getScheduler().runTaskLater(plugin, () -> ParticleEffects.ring(spawn, 1.2 + step * 0.18, Particle.DUST, ParticleEffects.GREEN_DUST, 32), pulse * 5L);
            }
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                spawn.getWorld().playSound(spawn, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.75f, 1.35f);
                spawn.getWorld().spawnParticle(Particle.END_ROD, spawn.clone().add(0, 0.8, 0), 26, 0.35, 0.5, 0.35, 0.04);
            }, 14L);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                spawn.getWorld().spawnParticle(Particle.EXPLOSION, spawn.clone().add(0, 0.35, 0), 1, 0.15, 0.15, 0.15, 0);
                spawn.getWorld().spawnParticle(Particle.DUST, spawn.clone().add(0, 0.6, 0), 55, 0.45, 0.45, 0.45, 0, ParticleEffects.GREEN_DUST);
                mobManager.spawn(ThreadLocalRandom.current().nextBoolean() ? ForestMobType.FOREST_SLIME : ForestMobType.FOREST_WOLF, spawn);
            }, 20L);
        }
        loc.getWorld().playSound(loc, Sound.BLOCK_GRASS_BREAK, 1.0f, 0.6f);
    }

    private void shockwave(LivingEntity boss) {
        Location center = boss.getLocation().clone();
        warnNearby(boss, "숲의 수호자가 광역 충격파를 모읍니다! 멀어지세요!");
        boss.getWorld().playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, 0.9f, 0.7f);
        for (int i = 1; i <= 7; i++) {
            final double radius = i * 1.15;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                ParticleEffects.ring(center, radius, Particle.DUST, ParticleEffects.WOOD_DUST, 28 + (int) (radius * 8));
                center.getWorld().spawnParticle(Particle.BLOCK, center.clone().add(0, 0.05, 0), 10, radius * 0.22, 0.02, radius * 0.22, 0.02, org.bukkit.Material.PODZOL.createBlockData());
            }, i * 6L);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!boss.isValid() || boss.isDead()) return;
            center.getWorld().spawnParticle(Particle.SONIC_BOOM, center.add(0, 1.0, 0), 1, 0, 0, 0, 0);
            center.getWorld().playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.85f, 1.25f);
            center.getWorld().getNearbyEntities(center, 7.8, 3.0, 7.8).stream()
                    .filter(e -> e instanceof Player)
                    .map(e -> (Player) e)
                    .filter(player -> player.getLocation().distanceSquared(center) <= 7.8 * 7.8)
                    .forEach(player -> {
                        player.damage(22.0, boss);
                        Vector push = player.getLocation().toVector().subtract(center.toVector()).normalize().multiply(1.25).setY(0.25);
                        player.setVelocity(push);
                    });
        }, 48L);
    }

    public boolean isBoss(LivingEntity entity) {
        return ID.equals(entity.getPersistentDataContainer().get(keys.bossId, PersistentDataType.STRING));
    }

    private void cleanupBar() {
        if (barTask != null) barTask.cancel();
        if (bossBar != null) bossBar.removeAll();
        activeBossId = null;
    }
}
