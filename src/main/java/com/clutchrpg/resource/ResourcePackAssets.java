package com.clutchrpg.resource;

import com.clutchrpg.items.Rarity;
import com.clutchrpg.items.WeaponType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public final class ResourcePackAssets {
    private final Plugin plugin;

    public ResourcePackAssets(Plugin plugin) {
        this.plugin = plugin;
    }

    public int weaponModelData(WeaponType weaponType, Rarity rarity) {
        String base = "resource-pack.items.weapons." + weaponType.name().toLowerCase();
        return config().getInt(base + "." + rarity.name().toLowerCase(), config().getInt(base + ".default", 0));
    }

    public int guiModelData(String iconId) {
        return config().getInt("resource-pack.gui.icons." + iconId, 0);
    }

    public int skillEffectModelData(String effectId) {
        return config().getInt("resource-pack.effects." + effectId, 0);
    }

    public String mythicMobId(String mobId) {
        return config().getString("resource-pack.mobs." + mobId + ".mythicMobId", mobId);
    }

    public String modelEngineId(String mobId) {
        return config().getString("resource-pack.mobs." + mobId + ".modelEngineId", mobId);
    }

    public String bossModelEngineId(String bossId) {
        return config().getString("resource-pack.bosses." + bossId + ".modelEngineId", bossId);
    }

    private FileConfiguration config() {
        return plugin.getConfig();
    }
}
