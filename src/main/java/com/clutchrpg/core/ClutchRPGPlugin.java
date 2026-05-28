package com.clutchrpg.core;

import com.clutchrpg.bosses.BossDeathListener;
import com.clutchrpg.bosses.ForestGuardianBoss;
import com.clutchrpg.combat.CombatListener;
import com.clutchrpg.combat.DamageService;
import com.clutchrpg.combat.DashListener;
import com.clutchrpg.combat.PotionListener;
import com.clutchrpg.commands.AdminCommand;
import com.clutchrpg.commands.PlayerCommands;
import com.clutchrpg.gui.StatsGui;
import com.clutchrpg.items.DropService;
import com.clutchrpg.items.ItemFactory;
import com.clutchrpg.mobs.MobDeathListener;
import com.clutchrpg.mobs.MobManager;
import com.clutchrpg.player.PlayerLifecycleListener;
import com.clutchrpg.player.PlayerManager;
import com.clutchrpg.storage.SQLiteStorage;
import com.clutchrpg.util.Chat;
import com.clutchrpg.util.CooldownTracker;
import com.clutchrpg.util.Keys;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ClutchRPGPlugin extends JavaPlugin {
    private SQLiteStorage storage;
    private PlayerManager playerManager;

    @Override public void onEnable() {
        saveDefaultConfig();
        Keys keys = new Keys(this);
        storage = new SQLiteStorage(this);
        storage.open();
        playerManager = new PlayerManager(storage);
        CooldownTracker cooldowns = new CooldownTracker();
        ItemFactory itemFactory = new ItemFactory(keys);
        DamageService damageService = new DamageService(playerManager, itemFactory);
        MobManager mobManager = new MobManager(keys);
        DropService dropService = new DropService(playerManager, itemFactory);
        ForestGuardianBoss forestGuardianBoss = new ForestGuardianBoss(this, keys, mobManager);
        StatsGui statsGui = new StatsGui(playerManager);

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerLifecycleListener(playerManager, itemFactory), this);
        pm.registerEvents(statsGui, this);
        pm.registerEvents(new CombatListener(itemFactory, damageService, cooldowns), this);
        pm.registerEvents(new DashListener(cooldowns), this);
        pm.registerEvents(new PotionListener(cooldowns), this);
        pm.registerEvents(new MobDeathListener(mobManager, playerManager, dropService), this);
        pm.registerEvents(new BossDeathListener(forestGuardianBoss, playerManager, dropService), this);

        PlayerCommands playerCommands = new PlayerCommands(statsGui);
        requireCommand("스텟").setExecutor(playerCommands);
        requireCommand("가방").setExecutor(playerCommands);
        requireCommand("도움말").setExecutor(playerCommands);
        AdminCommand adminCommand = new AdminCommand(this, mobManager, forestGuardianBoss, itemFactory, dropService);
        PluginCommand crpg = requireCommand("crpg");
        crpg.setExecutor(adminCommand);
        crpg.setTabCompleter(adminCommand);

        Bukkit.getOnlinePlayers().forEach(playerManager::load);
        getComponentLogger().info(Chat.PREFIX.append(Chat.text("ClutchRPG enabled - forest combat MVP ready.")));
    }

    @Override public void onDisable() {
        if (playerManager != null) playerManager.saveAll(Bukkit.getOnlinePlayers());
        if (storage != null) storage.close();
    }

    private PluginCommand requireCommand(String name) {
        PluginCommand command = getCommand(name);
        if (command == null) throw new IllegalStateException("plugin.yml command missing: " + name);
        return command;
    }
}
