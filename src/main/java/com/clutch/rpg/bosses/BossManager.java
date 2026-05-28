package com.clutch.rpg.bosses;

import com.clutch.rpg.items.CustomItemFactory;
import com.clutch.rpg.items.Rarity;
import com.clutch.rpg.items.WeaponType;
import com.clutch.rpg.mobs.ForestMobType;
import com.clutch.rpg.mobs.MobManager;
import com.clutch.rpg.player.PlayerDataManager;
import com.clutch.rpg.util.Keys;
import com.clutch.rpg.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class BossManager implements Listener {
    public static final String FOREST_GUARDIAN = "forest_guardian";
    private final Plugin plugin;
    private final Keys keys;
    private final PlayerDataManager playerDataManager;
    private final CustomItemFactory itemFactory;
    private final MobManager mobManager;
    private final Random random = new Random();

    public BossManager(Plugin plugin, Keys keys, PlayerDataManager playerDataManager, CustomItemFactory itemFactory, MobManager mobManager) {
        this.plugin = plugin;
        this.keys = keys;
        this.playerDataManager = playerDataManager;
        this.itemFactory = itemFactory;
        this.mobManager = mobManager;
    }

    public LivingEntity spawnForestGuardian(Location location) {
        LivingEntity boss = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.RAVAGER);
        boss.setCustomName(ChatColor.DARK_PURPLE + "숲의 수호자" + ChatColor.GRAY + " Lv.10");
        boss.setCustomNameVisible(true);
        if (boss.getAttribute(Attribute.MAX_HEALTH) != null) {
            boss.getAttribute(Attribute.MAX_HEALTH).setBaseValue(650.0);
        }
        boss.setHealth(650.0);
        boss.getPersistentDataContainer().set(keys.bossId, PersistentDataType.STRING, FOREST_GUARDIAN);
        runPatterns(boss);
        return boss;
    }

    private void runPatterns(LivingEntity boss) {
        new BukkitRunnable() {
            private int tick;
            @Override
            public void run() {
                if (!boss.isValid() || boss.isDead()) {
                    cancel();
                    return;
                }
                tick += 60;
                if (tick % 180 == 0) {
                    areaAttack(boss);
                } else if (tick % 300 == 0) {
                    summon(boss);
                } else {
                    slam(boss);
                }
            }
        }.runTaskTimer(plugin, 80L, 60L);
    }

    private void areaAttack(LivingEntity boss) {
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.0f, 0.8f);
        boss.getWorld().spawnParticle(Particle.SCRAPE, boss.getLocation().add(0, 1, 0), 80, 4, 0.4, 4, 0.05);
        nearbyPlayers(boss, 5.5).forEach(player -> player.damage(8.0, boss));
    }

    private void summon(LivingEntity boss) {
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_EVOKER_PREPARE_SUMMON, 1.0f, 0.9f);
        for (int i = 0; i < 3; i++) {
            Location spawn = boss.getLocation().clone().add(random.nextDouble() * 5 - 2.5, 0, random.nextDouble() * 5 - 2.5);
            mobManager.spawn(ForestMobType.GOBLIN, spawn);
        }
    }

    private void slam(LivingEntity boss) {
        Player target = nearbyPlayers(boss, 12.0).stream().min(Comparator.comparingDouble(player -> player.getLocation().distanceSquared(boss.getLocation()))).orElse(null);
        if (target == null) {
            return;
        }
        boss.getWorld().spawnParticle(Particle.LARGE_SMOKE, target.getLocation(), 30, 1.2, 0.2, 1.2, 0.02);
        new BukkitRunnable() {
            @Override
            public void run() {
                boss.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.9f, 0.8f);
                boss.getWorld().spawnParticle(Particle.EXPLOSION, target.getLocation(), 2, 0.4, 0.2, 0.4, 0.01);
                nearbyPlayersAt(target.getLocation(), 2.4).forEach(player -> player.damage(14.0, boss));
            }
        }.runTaskLater(plugin, 25L);
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        String bossId = event.getEntity().getPersistentDataContainer().get(keys.bossId, PersistentDataType.STRING);
        if (!FOREST_GUARDIAN.equals(bossId)) {
            return;
        }
        event.getDrops().clear();
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            playerDataManager.addExp(killer, 700);
            ItemStack reward = itemFactory.createWeapon(WeaponType.values()[random.nextInt(3)], Rarity.EPIC, 5);
            killer.getInventory().addItem(reward).values().forEach(left -> killer.getWorld().dropItemNaturally(killer.getLocation(), left));
            killer.playSound(killer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 0.75f);
            killer.getWorld().spawnParticle(Particle.DRAGON_BREATH, killer.getLocation().add(0, 1, 0), 80, 0.8, 0.9, 0.8, 0.04);
            MessageUtil.send(killer, "&d숲의 수호자 처치! &7보상이 지급되었습니다.");
        }
    }

    private List<Player> nearbyPlayers(LivingEntity boss, double radius) {
        return boss.getNearbyEntities(radius, radius, radius).stream().filter(Player.class::isInstance).map(Player.class::cast).toList();
    }

    private List<Player> nearbyPlayersAt(Location location, double radius) {
        return location.getWorld().getNearbyEntities(location, radius, radius, radius).stream().filter(Player.class::isInstance).map(Player.class::cast).toList();
    }
}
