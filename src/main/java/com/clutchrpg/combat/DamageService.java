package com.clutchrpg.combat;

import com.clutchrpg.items.ItemFactory;
import com.clutchrpg.player.PlayerManager;
import com.clutchrpg.player.PlayerProfile;
import com.clutchrpg.stats.StatCalculator;
import java.util.Random;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public final class DamageService {
    private final PlayerManager playerManager;
    private final ItemFactory itemFactory;
    private final Random random = new Random();

    public DamageService(PlayerManager playerManager, ItemFactory itemFactory) {
        this.playerManager = playerManager;
        this.itemFactory = itemFactory;
    }

    public double attack(Player player, LivingEntity target, double coefficient, boolean skill) {
        PlayerProfile profile = playerManager.get(player);
        double base = itemFactory.weaponDamage(player.getInventory().getItemInMainHand()) + StatCalculator.physicalPower(profile);
        double damage = base * coefficient;
        if (skill) damage *= 1.0 + StatCalculator.skillDamageBonus(profile);
        boolean crit = random.nextDouble() < StatCalculator.critChance(profile);
        if (crit) damage *= StatCalculator.BASE_CRIT_DAMAGE;
        target.damage(damage, player);
        target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1.0, 0), crit ? 18 : 8, 0.35, 0.4, 0.35, 0.05);
        target.getWorld().playSound(target.getLocation(), crit ? Sound.ENTITY_PLAYER_ATTACK_CRIT : Sound.ENTITY_PLAYER_ATTACK_STRONG, 0.7f, crit ? 1.25f : 1.05f);
        return damage;
    }

    public void applyStatus(Player player, LivingEntity target, StatusEffectType type, double baseDamage) {
        double bonus = 1.0 + StatCalculator.statusDamageBonus(playerManager.get(player));
        switch (type) {
            case SHOCK -> {
                target.damage(baseDamage * bonus, player);
                target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.03);
            }
            case FROST -> {
                target.setVelocity(target.getVelocity().multiply(0.35));
                target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 0.8, 0), 18, 0.35, 0.3, 0.35, new Particle.DustOptions(Color.AQUA, 1.2f));
            }
            case BLEED, BURN -> {
                target.damage(baseDamage * bonus, player);
                target.getWorld().spawnParticle(type == StatusEffectType.BURN ? Particle.FLAME : Particle.DAMAGE_INDICATOR, target.getLocation().add(0, 1, 0), 12, 0.35, 0.4, 0.35, 0.02);
            }
        }
    }
}
