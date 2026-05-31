package com.clutchrpg.combat;

import com.clutchrpg.items.ItemFactory;
import com.clutchrpg.items.WeaponType;
import com.clutchrpg.util.Chat;
import com.clutchrpg.util.CooldownTracker;
import com.clutchrpg.util.ParticleEffects;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import net.kyori.adventure.text.Component;
import org.bukkit.util.Vector;

public final class CombatListener implements Listener {
    private final Plugin plugin;
    private final ItemFactory itemFactory;
    private final DamageService damageService;
    private final CooldownTracker cooldowns;
    private final Map<UUID, Integer> swordCombo = new HashMap<>();
    private final Map<UUID, Long> comboExpires = new HashMap<>();

    public CombatListener(Plugin plugin, ItemFactory itemFactory, DamageService damageService, CooldownTracker cooldowns) {
        this.plugin = plugin;
        this.itemFactory = itemFactory;
        this.damageService = damageService;
        this.cooldowns = cooldowns;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (!(action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) return;
        Player player = event.getPlayer();
        WeaponType type = itemFactory.weaponType(player.getInventory().getItemInMainHand());
        if (type == null) return;
        event.setCancelled(true);
        switch (type) {
            case SWORD -> sword(player);
            case BOW -> bow(player);
            case STAFF -> staff(player);
            default -> { }
        }
    }

    private void sword(Player player) {
        if (!cooldowns.isReady(player.getUniqueId(), "sword")) return;
        cooldowns.set(player.getUniqueId(), "sword", Duration.ofMillis(295));
        long now = System.currentTimeMillis();
        int combo = comboExpires.getOrDefault(player.getUniqueId(), 0L) < now ? 1 : swordCombo.getOrDefault(player.getUniqueId(), 0) % 3 + 1;
        swordCombo.put(player.getUniqueId(), combo);
        comboExpires.put(player.getUniqueId(), now + 1150);

        Location origin = player.getLocation().add(0, 0.15, 0);
        Vector direction = player.getLocation().getDirection().normalize();
        double radius = combo == 2 ? 5.1 : combo == 3 ? 4.5 : 3.8;
        double arc = combo == 2 ? 125.0 : combo == 3 ? 105.0 : 82.0;
        double coeff = combo == 1 ? 0.88 : combo == 2 ? 1.10 : 1.62;

        player.sendActionBar(Chat.PREFIX.append(Component.text(combo == 1 ? "검 콤보 I - 짧은 베기" : combo == 2 ? "검 콤보 II - 반월 베기" : "검 콤보 III - 내려베기", combo == 3 ? Chat.PURPLE : Chat.GRAY)));
        if (combo == 1) {
            Location start = origin.clone().add(direction.clone().multiply(0.8)).add(0, 1.15, 0);
            Location end = origin.clone().add(direction.clone().multiply(3.0)).add(0, 0.72, 0);
            ParticleEffects.line(start, end, ParticleEffects.SILVER_DUST, 24);
            ParticleEffects.slashArc(origin, direction, 2.55, arc, ParticleEffects.SILVER_DUST, 20);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.48f, 1.55f);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_THROW, 0.30f, 1.75f);
        } else if (combo == 2) {
            Location liftStart = origin.clone().add(direction.clone().multiply(1.05)).add(0, 0.35, 0);
            Location liftEnd = origin.clone().add(direction.clone().multiply(3.6)).add(0, 1.85, 0);
            ParticleEffects.line(liftStart, liftEnd, ParticleEffects.GOLD_DUST, 34);
            ParticleEffects.slashArc(origin.clone().add(0, 0.25, 0), direction, 4.1, arc, ParticleEffects.SILVER_DUST, 40);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.78f, 0.95f);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_THROW, 0.42f, 1.18f);
        } else {
            ParticleEffects.verticalSlash(origin, direction, 4.15, 2.35, ParticleEffects.GOLD_DUST, 36);
            ParticleEffects.verticalSlash(origin.clone().add(0, 0.05, 0), direction, 3.5, 1.9, ParticleEffects.SILVER_DUST, 26);
            Location impact = origin.clone().add(direction.clone().multiply(3.15)).add(0, 0.15, 0);
            ParticleEffects.expandingRing(impact, 3.45, ParticleEffects.GOLD_DUST, 5, 1L, plugin);
            player.getWorld().spawnParticle(Particle.EXPLOSION, impact.clone().add(0, 0.55, 0), 2, 0.15, 0.15, 0.15, 0);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.55f, 1.45f);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.78f);
            player.setVelocity(player.getVelocity().add(direction.clone().multiply(-0.06).setY(0.03)));
        }

        player.getNearbyEntities(radius, 2.3, radius).stream()
                .filter(e -> e instanceof LivingEntity && e != player)
                .map(e -> (LivingEntity) e)
                .filter(target -> isInFrontCone(player, target, arc, radius))
                .limit(combo == 1 ? 2 : 6)
                .forEach(target -> {
                    damageService.attack(player, target, coeff, false);
                    target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1.0, 0), combo == 3 ? 18 : 9, 0.35, 0.35, 0.35, 0.05);
                    target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 1.0, 0), combo == 3 ? 16 : 8, 0.28, 0.28, 0.28, 0, combo == 3 ? ParticleEffects.GOLD_DUST : ParticleEffects.SILVER_DUST);
                    if (combo == 2) {
                        target.setVelocity(target.getVelocity().add(new Vector(0, 0.32, 0)));
                    }
                    if (combo == 3) {
                        Vector knockback = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(0.48).setY(0.24);
                        target.setVelocity(target.getVelocity().add(knockback));
                    }
                });
    }

    private boolean isInFrontCone(Player player, LivingEntity target, double degrees, double range) {
        Vector toTarget = target.getLocation().toVector().subtract(player.getLocation().toVector());
        if (toTarget.lengthSquared() > range * range) return false;
        Vector flatTarget = toTarget.setY(0).normalize();
        Vector flatForward = player.getLocation().getDirection().setY(0).normalize();
        return flatTarget.dot(flatForward) >= Math.cos(Math.toRadians(degrees / 2.0));
    }

    private void bow(Player player) {
        if (!cooldowns.isReady(player.getUniqueId(), "bow")) return;
        cooldowns.set(player.getUniqueId(), "bow", Duration.ofMillis(470));
        Snowball arrow = player.launchProjectile(Snowball.class);
        arrow.setVelocity(player.getLocation().getDirection().multiply(3.25));
        arrow.setGravity(false);
        arrow.addScoreboardTag("clutchrpg_energy_arrow");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.38f, 1.75f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GRASS_BREAK, 0.35f, 1.65f);
        trail(arrow.getUniqueId(), true);
    }

    private void staff(Player player) {
        if (!cooldowns.isReady(player.getUniqueId(), "staff")) return;
        cooldowns.set(player.getUniqueId(), "staff", Duration.ofMillis(600));
        Snowball ball = player.launchProjectile(Snowball.class);
        ball.setVelocity(player.getLocation().getDirection().multiply(1.75));
        ball.setGravity(false);
        ball.addScoreboardTag("clutchrpg_staff_orb");
        player.getWorld().spawnParticle(Particle.DUST, player.getEyeLocation(), 20, 0.22, 0.22, 0.22, 0, ParticleEffects.RED_DUST);
        player.getWorld().spawnParticle(Particle.DUST, player.getEyeLocation(), 20, 0.22, 0.22, 0.22, 0, ParticleEffects.PURPLE_DUST);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getEyeLocation(), 6, 0.15, 0.15, 0.15, 0.02);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.55f, 1.65f);
        trail(ball.getUniqueId(), false);
    }

    private void trail(UUID projectileId, boolean arrow) {
        new BukkitRunnable() {
            int ticks;
            @Override public void run() {
                var entity = org.bukkit.Bukkit.getEntity(projectileId);
                if (entity == null || entity.isDead() || ticks++ > 60) {
                    cancel();
                    return;
                }
                Location loc = entity.getLocation();
                loc.getWorld().spawnParticle(Particle.DUST, loc, arrow ? 7 : 9, 0.08, 0.08, 0.08, 0, arrow ? ParticleEffects.GREEN_DUST : ParticleEffects.RED_DUST);
                loc.getWorld().spawnParticle(Particle.DUST, loc.clone().add(0, 0.05, 0), arrow ? 3 : 9, 0.1, 0.1, 0.1, 0, arrow ? ParticleEffects.DARK_GREEN_DUST : ParticleEffects.PURPLE_DUST);
                loc.getWorld().spawnParticle(arrow ? Particle.HAPPY_VILLAGER : Particle.END_ROD, loc, arrow ? 2 : 3, 0.05, 0.05, 0.05, 0.01);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) return;
        if (event.getEntity().getScoreboardTags().contains("clutchrpg_energy_arrow")) {
            Location loc = event.getEntity().getLocation();
            loc.getWorld().spawnParticle(Particle.DUST, loc, 42, 0.35, 0.35, 0.35, 0, ParticleEffects.GREEN_DUST);
            loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 18, 0.25, 0.25, 0.25, 0.03);
            loc.getWorld().playSound(loc, Sound.BLOCK_GRASS_BREAK, 0.75f, 1.55f);
            if (event.getHitEntity() instanceof LivingEntity target) damageService.attack(player, target, 1.18, false);
            event.getEntity().remove();
        }
        if (event.getEntity().getScoreboardTags().contains("clutchrpg_staff_orb")) {
            Location loc = event.getEntity().getLocation();
            ParticleEffects.ring(loc, 2.2, Particle.DUST, ParticleEffects.RED_DUST, 42);
            ParticleEffects.ring(loc.clone().add(0, 0.08, 0), 1.35, Particle.DUST, ParticleEffects.PURPLE_DUST, 32);
            loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 3, 0.25, 0.25, 0.25, 0);
            loc.getWorld().spawnParticle(Particle.END_ROD, loc, 35, 0.55, 0.35, 0.55, 0.06);
            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.62f, 1.55f);
            loc.getWorld().getNearbyEntities(loc, 2.35, 2.35, 2.35).stream()
                    .filter(e -> e instanceof LivingEntity && e != player)
                    .map(e -> (LivingEntity) e)
                    .sorted(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(loc)))
                    .limit(5)
                    .forEach(target -> damageService.attack(player, target, 1.08, true));
            event.getEntity().remove();
        }
    }
}
