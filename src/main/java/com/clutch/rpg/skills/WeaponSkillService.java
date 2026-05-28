package com.clutch.rpg.skills;

import com.clutch.rpg.combat.CooldownService;
import com.clutch.rpg.combat.DamageCalculator;
import com.clutch.rpg.combat.StatusEffectService;
import com.clutch.rpg.combat.StatusEffectType;
import com.clutch.rpg.items.CustomItemFactory;
import com.clutch.rpg.items.WeaponType;
import com.clutch.rpg.player.PlayerDataManager;
import com.clutch.rpg.util.MessageUtil;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.RayTraceResult;

import java.time.Duration;

public class WeaponSkillService {
    private final Plugin plugin;
    private final CustomItemFactory itemFactory;
    private final PlayerDataManager playerDataManager;
    private final DamageCalculator damageCalculator;
    private final StatusEffectService statusEffectService;
    private final CooldownService cooldowns;

    public WeaponSkillService(Plugin plugin, CustomItemFactory itemFactory, PlayerDataManager playerDataManager, DamageCalculator damageCalculator, StatusEffectService statusEffectService, CooldownService cooldowns) {
        this.plugin = plugin;
        this.itemFactory = itemFactory;
        this.playerDataManager = playerDataManager;
        this.damageCalculator = damageCalculator;
        this.statusEffectService = statusEffectService;
        this.cooldowns = cooldowns;
    }

    public void cast(Player player, ItemStack item) {
        WeaponType type = itemFactory.getWeaponType(item);
        if (type == null) {
            return;
        }
        String key = "weapon_" + type.name();
        if (!cooldowns.ready(player.getUniqueId(), key)) {
            MessageUtil.send(player, "&7스킬 쿨타임: &b" + String.format("%.1f", cooldowns.remainingMillis(player.getUniqueId(), key) / 1000.0) + "초");
            return;
        }
        switch (type) {
            case SWORD -> swordArc(player, item);
            case BOW -> powerShot(player, item);
            case STAFF -> staffPulse(player, item);
            case HAMMER -> slam(player, item);
            case SHIELD -> guardPulse(player, item);
        }
        cooldowns.start(player.getUniqueId(), key, Duration.ofMillis(type == WeaponType.BOW ? 1800 : 2500));
    }

    private void swordArc(Player player, ItemStack item) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.15f);
        player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getLocation().add(player.getLocation().getDirection().multiply(1.5)).add(0, 1, 0), 3, 0.8, 0.4, 0.8, 0.01);
        hitCone(player, item, 4.0, 1.35, StatusEffectType.BLEED);
    }

    private void powerShot(Player player, ItemStack item) {
        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setVelocity(player.getLocation().getDirection().multiply(2.7));
        arrow.setCritical(true);
        DamageCalculator.DamageRoll roll = damageCalculator.roll(playerDataManager.get(player), item, 1.55);
        arrow.setDamage(roll.damage());
        arrow.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "skill_arrow"), org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 0.7f);
    }

    private void staffPulse(Player player, ItemStack item) {
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 0.8f);
        RayTraceResult result = player.rayTraceEntities(12.0, true);
        if (result != null && result.getHitEntity() instanceof LivingEntity target && target != player) {
            applyRoll(player, target, item, 1.65);
            statusEffectService.apply(target, StatusEffectType.SHOCK, 3.0, 1);
        }
        player.getWorld().spawnParticle(Particle.WITCH, player.getEyeLocation().add(player.getLocation().getDirection().multiply(2)), 25, 0.4, 0.4, 0.4, 0.02);
    }

    private void slam(Player player, ItemStack item) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 0.7f);
        hitCone(player, item, 3.5, 1.75, StatusEffectType.FROST);
    }

    private void guardPulse(Player player, ItemStack item) {
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 30, 1.0, 0.5, 1.0, 0.02);
        hitCone(player, item, 2.5, 0.85, StatusEffectType.SHOCK);
    }

    private void hitCone(Player player, ItemStack item, double range, double coefficient, StatusEffectType effect) {
        player.getNearbyEntities(range, range, range).stream()
                .filter(entity -> entity instanceof LivingEntity && entity != player)
                .map(LivingEntity.class::cast)
                .filter(target -> target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().dot(player.getLocation().getDirection()) > 0.25)
                .forEach(target -> {
                    applyRoll(player, target, item, coefficient);
                    statusEffectService.apply(target, effect, 2.0, 80);
                });
    }

    private void applyRoll(Player player, LivingEntity target, ItemStack item, double coefficient) {
        DamageCalculator.DamageRoll roll = damageCalculator.roll(playerDataManager.get(player), item, coefficient);
        target.damage(roll.damage(), player);
        if (roll.critical()) {
            target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 15, 0.35, 0.45, 0.35, 0.04);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.8f, 1.2f);
        }
    }
}
