package com.clutchrpg.combat;

import com.clutchrpg.items.ItemFactory;
import com.clutchrpg.items.WeaponType;
import com.clutchrpg.util.Chat;
import com.clutchrpg.util.CooldownTracker;
import com.clutchrpg.util.ParticleEffects;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
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
        double radius = combo == 2 ? 5.8 : combo == 3 ? 5.2 : 4.6;
        double arc = combo == 2 ? 140.0 : combo == 3 ? 120.0 : 110.0;
        double coeff = combo == 1 ? 0.88 : combo == 2 ? 1.10 : 1.62;

        player.sendActionBar(Chat.PREFIX.append(Component.text(combo == 1 ? "검 콤보 I - 짧은 베기" : combo == 2 ? "검 콤보 II - 반월 베기" : "검 콤보 III - 내려베기", combo == 3 ? Chat.PURPLE : Chat.GRAY)));
        if (combo == 1) {
            ParticleEffects.slashArc(origin.clone().add(0, 0.15, 0), direction, 3.8, arc, ParticleEffects.SILVER_DUST, 52);
            ParticleEffects.slashArc(origin.clone().add(0, 0.55, 0), direction, 3.25, arc * 0.82, ParticleEffects.GOLD_DUST, 36);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.70f, 1.35f);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_THROW, 0.42f, 1.65f);
        } else if (combo == 2) {
            Location liftStart = origin.clone().add(direction.clone().multiply(0.85)).add(0, 0.2, 0);
            Location liftEnd = origin.clone().add(direction.clone().multiply(4.35)).add(0, 2.45, 0);
            ParticleEffects.line(liftStart, liftEnd, ParticleEffects.GOLD_DUST, 48);
            ParticleEffects.line(liftStart.clone().add(0, 0.18, 0), liftEnd.clone().add(0, -0.12, 0), ParticleEffects.SILVER_DUST, 38);
            ParticleEffects.slashArc(origin.clone().add(0, 0.55, 0), direction, 4.8, arc, ParticleEffects.GOLD_DUST, 58);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.88f, 0.88f);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, 0.36f, 1.45f);
        } else {
            ParticleEffects.verticalSlash(origin, direction, 5.0, 2.85, ParticleEffects.GOLD_DUST, 54);
            ParticleEffects.verticalSlash(origin.clone().add(0, 0.05, 0), direction, 4.35, 2.35, ParticleEffects.SILVER_DUST, 42);
            Location impact = origin.clone().add(direction.clone().multiply(3.75)).add(0, 0.15, 0);
            ParticleEffects.expandingRing(impact, 4.25, ParticleEffects.GOLD_DUST, 6, 1L, plugin);
            ParticleEffects.expandingRing(impact.clone().add(0, 0.08, 0), 3.4, ParticleEffects.WOOD_DUST, 5, 1L, plugin);
            player.getWorld().spawnParticle(Particle.EXPLOSION, impact.clone().add(0, 0.55, 0), 3, 0.2, 0.2, 0.2, 0);
            player.getWorld().spawnParticle(Particle.DUST_PLUME, impact, 42, 1.8, 0.18, 1.8, 0.08);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.72f, 1.25f);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.68f);
            player.setVelocity(player.getVelocity().add(direction.clone().multiply(-0.08).setY(0.04)));
        }

        List<LivingEntity> targets = player.getNearbyEntities(radius, 2.8, radius).stream()
                .filter(e -> e instanceof LivingEntity && e != player)
                .map(e -> (LivingEntity) e)
                .filter(target -> isInFrontCone(player, target, arc, radius))
                .sorted(Comparator.comparingDouble(target -> target.getLocation().distanceSquared(player.getLocation())))
                .limit(combo == 1 ? 4 : 8)
                .toList();
        targets.forEach(target -> {
            double dealt = damageService.attack(player, target, coeff, false);
            playSwordHitFeedback(player, target, combo, dealt);
            if (combo == 2) {
                target.setVelocity(target.getVelocity().add(new Vector(0, 0.42, 0)));
            }
            if (combo == 3) {
                Vector away = target.getLocation().toVector().subtract(player.getLocation().toVector());
                if (away.lengthSquared() < 0.05) away = player.getLocation().getDirection();
                Vector knockback = away.normalize().multiply(0.58).setY(0.28);
                target.setVelocity(target.getVelocity().add(knockback));
            }
        });
    }

    private void playSwordHitFeedback(Player player, LivingEntity target, int combo, double dealt) {
        Location hit = target.getLocation().add(0, Math.max(0.7, target.getHeight() * 0.55), 0);
        target.getWorld().spawnParticle(Particle.CRIT, hit, combo == 3 ? 28 : combo == 2 ? 20 : 14, 0.42, 0.42, 0.42, 0.07);
        target.getWorld().spawnParticle(Particle.DUST, hit, combo == 3 ? 26 : 16, 0.34, 0.34, 0.34, 0, combo == 3 ? ParticleEffects.GOLD_DUST : ParticleEffects.SILVER_DUST);
        target.getWorld().playSound(hit, combo == 3 ? Sound.ENTITY_PLAYER_ATTACK_CRIT : Sound.ENTITY_PLAYER_ATTACK_STRONG, 0.85f, combo == 3 ? 0.72f : 1.05f);
        player.sendActionBar(Chat.PREFIX.append(Component.text("검 타격 " + String.format(java.util.Locale.KOREA, "%.1f", dealt), combo == 3 ? Chat.PURPLE : Chat.GRAY)));
    }

    private boolean isInFrontCone(Player player, LivingEntity target, double degrees, double range) {
        Vector toTarget = target.getLocation().toVector().subtract(player.getLocation().toVector());
        if (toTarget.lengthSquared() > range * range) return false;
        Vector flat = toTarget.clone().setY(0);
        if (flat.lengthSquared() < 0.35) return true;
        Vector flatTarget = flat.normalize();
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
