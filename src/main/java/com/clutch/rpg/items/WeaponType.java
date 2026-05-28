package com.clutch.rpg.items;

import org.bukkit.Material;

public enum WeaponType {
    SWORD(Material.IRON_SWORD, 8.0),
    BOW(Material.BOW, 6.0),
    STAFF(Material.BLAZE_ROD, 7.0),
    HAMMER(Material.IRON_AXE, 10.0),
    SHIELD(Material.SHIELD, 3.0);

    private final Material material;
    private final double baseAttack;

    WeaponType(Material material, double baseAttack) {
        this.material = material;
        this.baseAttack = baseAttack;
    }

    public Material material() { return material; }
    public double baseAttack() { return baseAttack; }
}
