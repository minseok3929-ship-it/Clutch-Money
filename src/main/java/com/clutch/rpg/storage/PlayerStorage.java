package com.clutch.rpg.storage;

import com.clutch.rpg.player.PlayerData;
import com.clutch.rpg.stats.StatBlock;
import com.clutch.rpg.stats.StatType;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.UUID;

public class PlayerStorage {
    private final File databaseFile;

    public PlayerStorage(File dataFolder) {
        this.databaseFile = new File(dataFolder, "players.db");
    }

    public void init() {
        databaseFile.getParentFile().mkdirs();
        try (Connection connection = connection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY, level INTEGER NOT NULL, exp INTEGER NOT NULL, stat_points INTEGER NOT NULL, str INTEGER NOT NULL, dex INTEGER NOT NULL, int_stat INTEGER NOT NULL, vit INTEGER NOT NULL, luk INTEGER NOT NULL)");
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to initialize SQLite player storage", exception);
        }
    }

    public Optional<PlayerData> load(UUID uuid) {
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM players WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }
                StatBlock stats = new StatBlock();
                stats.set(StatType.STR, result.getInt("str"));
                stats.set(StatType.DEX, result.getInt("dex"));
                stats.set(StatType.INT, result.getInt("int_stat"));
                stats.set(StatType.VIT, result.getInt("vit"));
                stats.set(StatType.LUK, result.getInt("luk"));
                return Optional.of(new PlayerData(uuid, result.getInt("level"), result.getLong("exp"), result.getInt("stat_points"), stats));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to load player " + uuid, exception);
        }
    }

    public void save(PlayerData data) {
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement("INSERT INTO players(uuid, level, exp, stat_points, str, dex, int_stat, vit, luk) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT(uuid) DO UPDATE SET level = excluded.level, exp = excluded.exp, stat_points = excluded.stat_points, str = excluded.str, dex = excluded.dex, int_stat = excluded.int_stat, vit = excluded.vit, luk = excluded.luk")) {
            statement.setString(1, data.uuid().toString());
            statement.setInt(2, data.level());
            statement.setLong(3, data.exp());
            statement.setInt(4, data.statPoints());
            statement.setInt(5, data.stats().get(StatType.STR));
            statement.setInt(6, data.stats().get(StatType.DEX));
            statement.setInt(7, data.stats().get(StatType.INT));
            statement.setInt(8, data.stats().get(StatType.VIT));
            statement.setInt(9, data.stats().get(StatType.LUK));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save player " + data.uuid(), exception);
        }
    }

    private Connection connection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
    }
}
