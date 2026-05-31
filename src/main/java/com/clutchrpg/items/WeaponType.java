package com.clutchrpg.items;

import org.bukkit.Material;

public enum WeaponType {
    SWORD("검", Material.IRON_SWORD, 12.0),
    BOW("활", Material.BOW, 9.0),
    STAFF("스태프", Material.BLAZE_ROD, 10.0),
    HAMMER("망치", Material.MACE, 16.0),
    SHIELD("방패", Material.SHIELD, 4.0);

    private final String korean;
    private final Material material;
    private final double baseDamage;

    WeaponType(String korean, Material material, double baseDamage) {
        this.korean = korean; this.material = material; this.baseDamage = baseDamage;
    }
    public String korean() { return korean; }
    public Material material() { return material; }
    public double baseDamage() { return baseDamage; }
}
