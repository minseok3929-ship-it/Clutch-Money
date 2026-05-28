package com.clutchrpg.combat;

import com.clutchrpg.items.ItemFactory;
import com.clutchrpg.items.WeaponType;
import com.clutchrpg.util.CooldownTracker;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public final class CombatListener implements Listener {
    private final ItemFactory itemFactory;
    private final DamageService damageService;
    private final CooldownTracker cooldowns;
    private final Map<UUID, Integer> swordCombo = new HashMap<>();
    private final Map<UUID, Long> comboExpires = new HashMap<>();

    public CombatListener(ItemFactory itemFactory, DamageService damageService, CooldownTracker cooldowns) {
        this.itemFactory = itemFactory; this.damageService = damageService; this.cooldowns = cooldowns;
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
        cooldowns.set(player.getUniqueId(), "sword", Duration.ofMillis(260));
        long now = System.currentTimeMillis();
        int combo = comboExpires.getOrDefault(player.getUniqueId(), 0L) < now ? 1 : swordCombo.getOrDefault(player.getUniqueId(), 0) % 3 + 1;
        swordCombo.put(player.getUniqueId(), combo);
        comboExpires.put(player.getUniqueId(), now + 1100);
        double radius = combo == 2 ? 3.2 : 2.5;
        double coeff = combo == 1 ? 0.85 : combo == 2 ? 1.05 : 1.55;
        Location origin = player.getEyeLocation().add(player.getLocation().getDirection().normalize().multiply(1.8));
        player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, origin, combo == 2 ? 4 : 2, 0.6, 0.2, 0.6, 0.0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.75f, combo == 3 ? 0.75f : 1.25f);
        player.getNearbyEntities(radius, 2.0, radius).stream()
                .filter(e -> e instanceof LivingEntity && e != player)
                .map(e -> (LivingEntity) e)
                .filter(target -> target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().dot(player.getLocation().getDirection()) > 0.25)
                .limit(combo == 1 ? 2 : 5)
                .forEach(target -> damageService.attack(player, target, coeff, false));
    }

    private void bow(Player player) {
        if (!cooldowns.isReady(player.getUniqueId(), "bow")) return;
        cooldowns.set(player.getUniqueId(), "bow", Duration.ofMillis(420));
        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setVelocity(player.getLocation().getDirection().multiply(2.8));
        arrow.setDamage(0.1);
        arrow.setCritical(true);
        arrow.addScoreboardTag("clutchrpg_arrow");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.8f, 1.8f);
    }

    private void staff(Player player) {
        if (!cooldowns.isReady(player.getUniqueId(), "staff")) return;
        cooldowns.set(player.getUniqueId(), "staff", Duration.ofMillis(520));
        SmallFireball ball = player.launchProjectile(SmallFireball.class);
        ball.setVelocity(player.getLocation().getDirection().multiply(1.55));
        ball.setIsIncendiary(false);
        ball.setYield(0);
        ball.addScoreboardTag("clutchrpg_staff");
        player.getWorld().spawnParticle(Particle.DUST, player.getEyeLocation(), 16, 0.2, 0.2, 0.2, new Particle.DustOptions(Color.PURPLE, 1.4f));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 0.7f, 1.35f);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) return;
        if (event.getEntity().getScoreboardTags().contains("clutchrpg_arrow")) {
            if (event.getHitEntity() instanceof LivingEntity target) damageService.attack(player, target, 1.15, false);
            event.getEntity().remove();
        }
        if (event.getEntity().getScoreboardTags().contains("clutchrpg_staff")) {
            Location loc = event.getEntity().getLocation();
            loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 2, 0.2, 0.2, 0.2, 0);
            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.55f, 1.45f);
            loc.getWorld().getNearbyEntities(loc, 2.2, 2.2, 2.2).stream()
                    .filter(e -> e instanceof LivingEntity && e != player)
                    .map(e -> (LivingEntity) e)
                    .sorted(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(loc)))
                    .limit(4)
                    .forEach(target -> damageService.attack(player, target, 1.05, true));
            event.getEntity().remove();
        }
    }
}
