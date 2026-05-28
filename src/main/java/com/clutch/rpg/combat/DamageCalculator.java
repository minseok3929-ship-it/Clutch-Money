package com.clutch.rpg.combat;

import com.clutch.rpg.items.CustomItemFactory;
import com.clutch.rpg.items.OptionType;
import com.clutch.rpg.player.PlayerData;
import com.clutch.rpg.stats.StatType;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class DamageCalculator {
    private static final double BASE_CRIT_DAMAGE = 1.5;
    private final CustomItemFactory itemFactory;
    private final Random random = new Random();

    public DamageCalculator(CustomItemFactory itemFactory) {
        this.itemFactory = itemFactory;
    }

    public DamageRoll roll(PlayerData data, ItemStack weapon, double skillCoefficient) {
        double finalAttack = itemFactory.baseAttack(weapon)
                + data.stats().scaled(StatType.STR) * 0.22
                + data.stats().scaled(StatType.DEX) * 0.12
                + data.stats().scaled(StatType.INT) * 0.20
                + itemFactory.optionSum(weapon, OptionType.ATTACK_DAMAGE);
        double skillBonus = 1.0 + itemFactory.optionSum(weapon, OptionType.SKILL_DAMAGE) / 100.0;
        double damage = finalAttack * skillCoefficient * skillBonus;
        double statCritChance = Math.min(35.0, data.stats().scaled(StatType.DEX) * 0.25 + data.stats().scaled(StatType.LUK) * 0.10);
        double totalCritChance = Math.min(100.0, statCritChance + itemFactory.optionSum(weapon, OptionType.CRIT_CHANCE));
        boolean crit = random.nextDouble() * 100.0 < totalCritChance;
        if (crit) {
            damage *= BASE_CRIT_DAMAGE + itemFactory.optionSum(weapon, OptionType.CRIT_DAMAGE) / 100.0;
        }
        double armorPen = itemFactory.optionSum(weapon, OptionType.ARMOR_PENETRATION);
        return new DamageRoll(Math.max(1.0, damage), crit, armorPen);
    }

    public record DamageRoll(double damage, boolean critical, double armorPenetration) {
    }
}
