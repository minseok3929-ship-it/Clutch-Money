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
            int maxAlive = point.getInt("maxAlive", defaultMaxAlive(type));
            double radius = point.getDouble("radius", defaultRadius(type));
            int respawnSeconds = point.getInt("respawnInterval", defaultRespawn(type));
            int batchMin = point.getInt("spawnBatchMin", defaultBatchMin(type));
            int batchMax = point.getInt("spawnBatchMax", defaultBatchMax(type));
            spawnPoints.add(new SpawnPoint(Integer.parseInt(key), type, new Location(world, point.getDouble("x"), point.getDouble("y"), point.getDouble("z")), maxAlive, radius, respawnSeconds, batchMin, batchMax));
        }
        spawnPoints.sort(Comparator.comparingInt(SpawnPoint::id));
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 40L, 40L);
    }

    public void stop() {
        if (task != null) task.cancel();
    }

    public SpawnPoint add(ForestMobType type, Location location, int maxAlive, double radius) {
        int id = spawnPoints.stream().mapToInt(SpawnPoint::id).max().orElse(0) + 1;
        SpawnPoint point = new SpawnPoint(id, type, location.toBlockLocation().add(0.5, 0, 0.5), maxAlive, radius, defaultRespawn(type), defaultBatchMin(type), defaultBatchMax(type));
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
            plugin.getConfig().set(path + ".maxAlive", point.safeMaxAlive());
            plugin.getConfig().set(path + ".radius", point.safeRadius());
            plugin.getConfig().set(path + ".respawnInterval", point.safeRespawnSeconds());
            plugin.getConfig().set(path + ".spawnBatchMin", point.safeBatchMin());
            plugin.getConfig().set(path + ".spawnBatchMax", point.safeBatchMax());
        }
        plugin.saveConfig();
    }

    private void tick() {
        long now = System.currentTimeMillis();
        for (SpawnPoint point : spawnPoints) {
            long alive = countAlive(point.id());
            if (alive >= point.safeMaxAlive()) continue;
            if (nextSpawnAt.getOrDefault(point.id(), 0L) > now) continue;

            int missing = point.safeMaxAlive() - (int) alive;
            int requested = ThreadLocalRandom.current().nextInt(point.safeBatchMin(), point.safeBatchMax() + 1);
            int count = Math.min(missing, requested);
            for (int i = 0; i < count; i++) {
                Location spawn = randomLocation(point);
                mobManager.spawn(point.mobType(), spawn, point.id());
            }
            nextSpawnAt.put(point.id(), now + point.safeRespawnSeconds() * 1000L);
        }
    }

    private Location randomLocation(SpawnPoint point) {
        double radius = point.safeRadius();
        double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2.0);
        double distance = Math.sqrt(ThreadLocalRandom.current().nextDouble()) * radius;
        Location base = point.location().clone().add(Math.cos(angle) * distance, 0, Math.sin(angle) * distance);
        World world = base.getWorld();
        int y = world.getHighestBlockYAt(base) + 1;
        base.setY(Math.max(point.location().getY(), y));
        return base;
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
            Chat.send(sender, "등록된 스폰 캠프가 없습니다.");
            return;
        }
        for (SpawnPoint point : spawnPoints) {
            Chat.send(sender, "#" + point.id() + " " + point.mobType().koreanName()
                    + " / 최대 " + point.safeMaxAlive() + " / 반경 " + (int) point.safeRadius()
                    + " / 배치 " + point.safeBatchMin() + "~" + point.safeBatchMax()
                    + " / " + point.location().getWorld().getName() + " " + point.location().getBlockX() + ", " + point.location().getBlockY() + ", " + point.location().getBlockZ());
        }
    }

    private int defaultMaxAlive(ForestMobType type) {
        return switch (type) {
            case FOREST_SLIME -> 10;
            case GOBLIN -> 6;
            case FOREST_WOLF -> 5;
            case VINE_GOLEM -> 3;
        };
    }

    private double defaultRadius(ForestMobType type) {
        return switch (type) {
            case FOREST_SLIME -> 10.0;
            case GOBLIN -> 8.0;
            case FOREST_WOLF -> 9.0;
            case VINE_GOLEM -> 7.0;
        };
    }

    private int defaultRespawn(ForestMobType type) {
        return switch (type) {
            case FOREST_SLIME -> 15;
            case GOBLIN -> 20;
            case FOREST_WOLF -> 18;
            case VINE_GOLEM -> 24;
        };
    }

    private int defaultBatchMin(ForestMobType type) {
        return switch (type) {
            case FOREST_SLIME -> 3;
            case GOBLIN -> 2;
            case FOREST_WOLF -> 1;
            case VINE_GOLEM -> 1;
        };
    }

    private int defaultBatchMax(ForestMobType type) {
        return switch (type) {
            case FOREST_SLIME -> 5;
            case GOBLIN -> 4;
            case FOREST_WOLF -> 2;
            case VINE_GOLEM -> 1;
        };
    }
}
