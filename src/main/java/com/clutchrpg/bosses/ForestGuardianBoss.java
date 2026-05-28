package com.clutchrpg.bosses;

import com.clutchrpg.mobs.ForestMobType;
import com.clutchrpg.mobs.MobManager;
import com.clutchrpg.util.Keys;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

public final class ForestGuardianBoss {
    public static final String ID = "forest_guardian";
    private final Plugin plugin;
    private final Keys keys;
    private final MobManager mobManager;
    private BukkitTask patternTask;

    public ForestGuardianBoss(Plugin plugin, Keys keys, MobManager mobManager) {
        this.plugin = plugin; this.keys = keys; this.mobManager = mobManager;
    }

    public LivingEntity spawn(Location location) {
        LivingEntity boss = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.RAVAGER);
        boss.customName(Component.text("숲의 수호자", NamedTextColor.DARK_GREEN));
        boss.setCustomNameVisible(true);
        boss.getAttribute(Attribute.MAX_HEALTH).setBaseValue(950.0);
        boss.setHealth(950.0);
        boss.getPersistentDataContainer().set(keys.bossId, PersistentDataType.STRING, ID);
        boss.setRemoveWhenFarAway(false);
        startPatterns(boss);
        return boss;
    }

    private void startPatterns(LivingEntity boss) {
        if (patternTask != null) patternTask.cancel();
        patternTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!boss.isValid() || boss.isDead()) {
                patternTask.cancel();
                return;
            }
            int pattern = ThreadLocalRandom.current().nextInt(3);
            if (pattern == 0) areaRoar(boss);
            if (pattern == 1) summon(boss);
            if (pattern == 2) slam(boss);
        }, 80L, 100L);
    }

    private void areaRoar(LivingEntity boss) {
        Location loc = boss.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_RAVAGER_ROAR, 1.2f, 0.85f);
        loc.getWorld().spawnParticle(Particle.SONIC_BOOM, loc.add(0, 1, 0), 1, 0, 0, 0, 0);
        boss.getNearbyEntities(7, 3, 7).stream().filter(e -> e instanceof Player).map(e -> (Player) e).forEach(player -> player.damage(18.0, boss));
    }

    private void summon(LivingEntity boss) {
        Location loc = boss.getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_GRASS_BREAK, 1.0f, 0.6f);
        for (int i = 0; i < 3; i++) {
            Location spawn = loc.clone().add(ThreadLocalRandom.current().nextDouble(-4, 4), 0, ThreadLocalRandom.current().nextDouble(-4, 4));
            mobManager.spawn(i == 0 ? ForestMobType.FOREST_WOLF : ForestMobType.GOBLIN, spawn);
        }
    }

    private void slam(LivingEntity boss) {
        Location loc = boss.getLocation();
        loc.getWorld().spawnParticle(Particle.DUST_PLUME, loc, 70, 4.5, 0.2, 4.5, 0.08);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.65f);
        boss.getNearbyEntities(4.5, 2.5, 4.5).stream().filter(e -> e instanceof Player).map(e -> (Player) e).forEach(player -> {
            player.damage(26.0, boss);
            player.setVelocity(player.getLocation().toVector().subtract(boss.getLocation().toVector()).normalize().multiply(1.1).setY(0.45));
        });
    }

    public boolean isBoss(LivingEntity entity) {
        return ID.equals(entity.getPersistentDataContainer().get(keys.bossId, PersistentDataType.STRING));
    }
}
