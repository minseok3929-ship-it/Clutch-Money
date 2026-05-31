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
import com.clutchrpg.mobs.MobBehaviorController;
import com.clutchrpg.mobs.MobDeathListener;
import com.clutchrpg.mobs.MobManager;
import com.clutchrpg.mobs.MobPresentationListener;
import com.clutchrpg.mobs.MobProjectileListener;
import com.clutchrpg.mobs.SpawnPointManager;
import com.clutchrpg.mobs.VanillaMobSpawnListener;
import com.clutchrpg.player.PlayerLifecycleListener;
import com.clutchrpg.player.PlayerManager;
import com.clutchrpg.resource.ResourcePackAssets;
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
    private SpawnPointManager spawnPointManager;
    private MobBehaviorController mobBehaviorController;

    @Override public void onEnable() {
        saveDefaultConfig();
        Keys keys = new Keys(this);
        storage = new SQLiteStorage(this);
        storage.open();
        playerManager = new PlayerManager(storage);
        CooldownTracker cooldowns = new CooldownTracker();
        ResourcePackAssets resourcePackAssets = new ResourcePackAssets(this);
        ItemFactory itemFactory = new ItemFactory(keys, resourcePackAssets);
        DamageService damageService = new DamageService(playerManager, itemFactory);
        MobManager mobManager = new MobManager(this, keys);
        spawnPointManager = new SpawnPointManager(this, mobManager);
        spawnPointManager.load();
        DropService dropService = new DropService(playerManager, itemFactory);
        ForestGuardianBoss forestGuardianBoss = new ForestGuardianBoss(this, keys, mobManager);
        StatsGui statsGui = new StatsGui(playerManager, resourcePackAssets);
        mobBehaviorController = new MobBehaviorController(this, mobManager);

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerLifecycleListener(playerManager, itemFactory), this);
        pm.registerEvents(statsGui, this);
        pm.registerEvents(new CombatListener(this, itemFactory, damageService, cooldowns), this);
        pm.registerEvents(new DashListener(cooldowns), this);
        pm.registerEvents(new PotionListener(cooldowns), this);
        pm.registerEvents(new MobDeathListener(mobManager, playerManager, dropService), this);
        pm.registerEvents(new MobPresentationListener(this, mobManager), this);
        pm.registerEvents(new MobProjectileListener(), this);
        pm.registerEvents(new VanillaMobSpawnListener(), this);
        pm.registerEvents(new BossDeathListener(forestGuardianBoss, playerManager, dropService), this);

        PlayerCommands playerCommands = new PlayerCommands(statsGui);
        requireCommand("스텟").setExecutor(playerCommands);
        requireCommand("가방").setExecutor(playerCommands);
        requireCommand("도움말").setExecutor(playerCommands);
        AdminCommand adminCommand = new AdminCommand(this, mobManager, spawnPointManager, forestGuardianBoss, itemFactory, dropService);
        PluginCommand crpg = requireCommand("crpg");
        crpg.setExecutor(adminCommand);
        crpg.setTabCompleter(adminCommand);

        purgeVanillaHostiles(mobManager);
        spawnPointManager.start();
        mobBehaviorController.start();
        Bukkit.getOnlinePlayers().forEach(playerManager::load);
        getComponentLogger().info(Chat.PREFIX.append(Chat.text("ClutchRPG enabled - visual forest combat MVP ready.")));
    }

    private void purgeVanillaHostiles(MobManager mobManager) {
        Bukkit.getWorlds().forEach(world -> world.getLivingEntities().stream()
                .filter(entity -> entity instanceof org.bukkit.entity.Monster || entity instanceof org.bukkit.entity.Slime)
                .filter(entity -> mobManager.typeOf(entity) == null)
                .forEach(org.bukkit.entity.Entity::remove));
    }

    @Override public void onDisable() {
        if (spawnPointManager != null) spawnPointManager.stop();
        if (mobBehaviorController != null) mobBehaviorController.stop();
        if (playerManager != null) playerManager.saveAll(Bukkit.getOnlinePlayers());
        if (storage != null) storage.close();
    }

    private PluginCommand requireCommand(String name) {
        PluginCommand command = getCommand(name);
        if (command == null) throw new IllegalStateException("plugin.yml command missing: " + name);
        return command;
    }
}
