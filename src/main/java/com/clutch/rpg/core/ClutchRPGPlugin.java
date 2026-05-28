package com.clutch.rpg.core;

import com.clutch.rpg.bosses.BossManager;
import com.clutch.rpg.combat.CombatListener;
import com.clutch.rpg.combat.CooldownService;
import com.clutch.rpg.combat.DamageCalculator;
import com.clutch.rpg.combat.DashService;
import com.clutch.rpg.combat.StatusEffectService;
import com.clutch.rpg.commands.ClutchRPGCommand;
import com.clutch.rpg.items.CustomItemFactory;
import com.clutch.rpg.mobs.MobManager;
import com.clutch.rpg.player.PlayerDataManager;
import com.clutch.rpg.player.PlayerLifecycleListener;
import com.clutch.rpg.skills.WeaponSkillService;
import com.clutch.rpg.storage.PlayerStorage;
import com.clutch.rpg.util.Keys;
import com.clutch.rpg.util.MessageUtil;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class ClutchRPGPlugin extends JavaPlugin {
    private PlayerDataManager playerDataManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Keys keys = new Keys(this);
        PlayerStorage storage = new PlayerStorage(getDataFolder());
        storage.init();
        playerDataManager = new PlayerDataManager(storage);
        CustomItemFactory itemFactory = new CustomItemFactory(keys);
        DamageCalculator damageCalculator = new DamageCalculator(itemFactory);
        CooldownService cooldownService = new CooldownService();
        StatusEffectService statusEffectService = new StatusEffectService(this);
        DashService dashService = new DashService(this, cooldownService, itemFactory);
        WeaponSkillService weaponSkillService = new WeaponSkillService(this, itemFactory, playerDataManager, damageCalculator, statusEffectService, cooldownService);
        MobManager mobManager = new MobManager(this, keys, playerDataManager, itemFactory);
        BossManager bossManager = new BossManager(this, keys, playerDataManager, itemFactory, mobManager);

        getServer().getPluginManager().registerEvents(new PlayerLifecycleListener(playerDataManager), this);
        getServer().getPluginManager().registerEvents(new CombatListener(itemFactory, playerDataManager, damageCalculator, weaponSkillService, dashService), this);
        getServer().getPluginManager().registerEvents(mobManager, this);
        getServer().getPluginManager().registerEvents(bossManager, this);
        getServer().getOnlinePlayers().forEach(playerDataManager::load);

        ClutchRPGCommand command = new ClutchRPGCommand(this, playerDataManager, itemFactory, mobManager, bossManager);
        PluginCommand pluginCommand = getCommand("crpg");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }
        getLogger().info(MessageUtil.PREFIX + "ClutchRPG dense forest combat prototype enabled.");
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
    }
}
