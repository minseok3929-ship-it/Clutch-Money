package com.clutchrpg.mobs;

import com.clutchrpg.util.Chat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class SpawnPointManager {
    private final Plugin plugin;
    private final MobManager mobManager;
    private final List<SpawnPoint> spawnPoints = new ArrayList<>();
    private final Map<Integer, Long> nextSpawnAt = new HashMap<>();
    private BukkitTask task;

    public SpawnPointManager(Plugin plugin, MobManager mobManager) {
        this.plugin = plugin;
        this.mobManager = mobManager;
    }

    public void load() {
        spawnPoints.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("forest.spawn-points");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            ConfigurationSection point = section.getConfigurationSection(key);
            if (point == null) continue;
            World world = Bukkit.getWorld(point.getString("world", "world"));
            ForestMobType type = ForestMobType.byId(point.getString("mob", "forest_slime"));
            if (world == null || type == null) continue;
            spawnPoints.add(new SpawnPoint(Integer.parseInt(key), type, new Location(world, point.getDouble("x"), point.getDouble("y"), point.getDouble("z"))));
        }
        spawnPoints.sort(Comparator.comparingInt(SpawnPoint::id));
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 40L, 40L);
    }

    public void stop() {
        if (task != null) task.cancel();
    }

    public SpawnPoint add(ForestMobType type, Location location) {
        int id = spawnPoints.stream().mapToInt(SpawnPoint::id).max().orElse(0) + 1;
        SpawnPoint point = new SpawnPoint(id, type, location.toBlockLocation().add(0.5, 0, 0.5));
        spawnPoints.add(point);
        save();
        return point;
    }

    public boolean remove(int id) {
        boolean removed = spawnPoints.removeIf(point -> point.id() == id);
        if (removed) save();
        return removed;
    }

    public List<SpawnPoint> list() {
        return List.copyOf(spawnPoints);
    }

    public void reload() {
        load();
        nextSpawnAt.clear();
    }

    private void save() {
        plugin.getConfig().set("forest.spawn-points", null);
        for (SpawnPoint point : spawnPoints) {
            String path = "forest.spawn-points." + point.id();
            plugin.getConfig().set(path + ".mob", point.mobType().id());
            plugin.getConfig().set(path + ".world", point.location().getWorld().getName());
            plugin.getConfig().set(path + ".x", point.location().getX());
            plugin.getConfig().set(path + ".y", point.location().getY());
            plugin.getConfig().set(path + ".z", point.location().getZ());
        }
        plugin.saveConfig();
    }

    private void tick() {
        int max = plugin.getConfig().getInt("forest.spawn.max-per-point", 5);
        int minSeconds = plugin.getConfig().getInt("forest.spawn.respawn-min-seconds", 15);
        int maxSeconds = plugin.getConfig().getInt("forest.spawn.respawn-max-seconds", 25);
        long now = System.currentTimeMillis();
        for (SpawnPoint point : spawnPoints) {
            if (countAlive(point.id()) >= max) continue;
            if (nextSpawnAt.getOrDefault(point.id(), 0L) > now) continue;
            Location spawn = point.location().clone().add(ThreadLocalRandom.current().nextDouble(-3.5, 3.5), 0, ThreadLocalRandom.current().nextDouble(-3.5, 3.5));
            mobManager.spawn(point.mobType(), spawn, point.id());
            int delay = ThreadLocalRandom.current().nextInt(Math.min(minSeconds, maxSeconds), Math.max(minSeconds, maxSeconds) + 1);
            nextSpawnAt.put(point.id(), now + delay * 1000L);
        }
    }

    private long countAlive(int pointId) {
        return Bukkit.getWorlds().stream()
                .flatMap(world -> world.getLivingEntities().stream())
                .filter(entity -> mobManager.spawnPointId(entity) == pointId)
                .filter(entity -> entity.isValid() && !entity.isDead())
                .count();
    }

    public void sendList(org.bukkit.command.CommandSender sender) {
        if (spawnPoints.isEmpty()) {
            Chat.send(sender, "등록된 스폰 포인트가 없습니다.");
            return;
        }
        for (SpawnPoint point : spawnPoints) {
            Chat.send(sender, "#" + point.id() + " " + point.mobType().koreanName() + " @ " + point.location().getWorld().getName() + " " + point.location().getBlockX() + ", " + point.location().getBlockY() + ", " + point.location().getBlockZ());
        }
    }
}
