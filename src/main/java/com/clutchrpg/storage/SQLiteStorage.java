package com.clutchrpg.storage;

import com.clutchrpg.player.PlayerProfile;
import com.clutchrpg.stats.StatType;
import java.io.File;
import java.sql.*;
import java.util.UUID;
import org.bukkit.plugin.Plugin;

public final class SQLiteStorage implements AutoCloseable {
    private final File databaseFile;
    private Connection connection;

    public SQLiteStorage(Plugin plugin) {
        this.databaseFile = new File(plugin.getDataFolder(), "players.db");
    }

    public void open() {
        try {
            File parent = databaseFile.getParentFile();
            if (!parent.exists()) parent.mkdirs();
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            try (Statement st = connection.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY, level INTEGER NOT NULL, exp INTEGER NOT NULL, stat_points INTEGER NOT NULL, str INTEGER NOT NULL, dex INTEGER NOT NULL, int_stat INTEGER NOT NULL, vit INTEGER NOT NULL, luk INTEGER NOT NULL)");
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("SQLite 초기화 실패", ex);
        }
    }

    public PlayerProfile load(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM players WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return new PlayerProfile(uuid);
                PlayerProfile profile = new PlayerProfile(uuid);
                profile.setLevel(rs.getInt("level"));
                profile.setExp(rs.getLong("exp"));
                profile.setStatPoints(rs.getInt("stat_points"));
                profile.setStat(StatType.STR, rs.getInt("str"));
                profile.setStat(StatType.DEX, rs.getInt("dex"));
                profile.setStat(StatType.INT, rs.getInt("int_stat"));
                profile.setStat(StatType.VIT, rs.getInt("vit"));
                profile.setStat(StatType.LUK, rs.getInt("luk"));
                return profile;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("플레이어 데이터 로드 실패: " + uuid, ex);
        }
    }

    public void save(PlayerProfile profile) {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO players(uuid,level,exp,stat_points,str,dex,int_stat,vit,luk) VALUES(?,?,?,?,?,?,?,?,?) ON CONFLICT(uuid) DO UPDATE SET level=excluded.level, exp=excluded.exp, stat_points=excluded.stat_points, str=excluded.str, dex=excluded.dex, int_stat=excluded.int_stat, vit=excluded.vit, luk=excluded.luk")) {
            ps.setString(1, profile.uuid().toString());
            ps.setInt(2, profile.level());
            ps.setLong(3, profile.exp());
            ps.setInt(4, profile.statPoints());
            ps.setInt(5, profile.stat(StatType.STR));
            ps.setInt(6, profile.stat(StatType.DEX));
            ps.setInt(7, profile.stat(StatType.INT));
            ps.setInt(8, profile.stat(StatType.VIT));
            ps.setInt(9, profile.stat(StatType.LUK));
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("플레이어 데이터 저장 실패: " + profile.uuid(), ex);
        }
    }

    @Override public void close() {
        if (connection != null) {
            try { connection.close(); } catch (SQLException ignored) { }
        }
    }
}
