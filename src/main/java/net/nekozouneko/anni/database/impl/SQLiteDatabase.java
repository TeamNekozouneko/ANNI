package net.nekozouneko.anni.database.impl;

import net.nekozouneko.anni.ANNIConfig;
import net.nekozouneko.anni.database.ANNIDatabase;
import net.nekozouneko.anni.kit.ANNIKit;
import net.nekozouneko.anni.kit.AbstractKit;
import net.nekozouneko.anni.util.CmnUtil;
import net.nekozouneko.anni.util.FileUtil;

import java.io.File;
import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SQLiteDatabase implements ANNIDatabase {

    private Connection source;

    public SQLiteDatabase() {
        new File(ANNIConfig.getLocalDBPath()).getParentFile().mkdirs();

        connect();
        createTable("level", "player TEXT NOT NULL PRIMARY KEY, level integer, exp integer");
        createTable("settings", "player TEXT NOT NULL PRIMARY KEY, kit TEXT");
        createTable("available_kits", "player TEXT NOT NULL PRIMARY KEY, kits TEXT NOT NULL DEFAULT '[]'");
        createTable("statistic", "player TEXT NOT NULL PRIMARY KEY, kills integer, deaths integer, nexus integer, wins integer, loses integer");
    }

    private void connect() {
        try {
            source = DriverManager.getConnection("jdbc:sqlite:" + ANNIConfig.getLocalDBPath());
            source.setAutoCommit(true);
        }
        catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    private void createTable(String tableName, String args) {
        try (PreparedStatement ps = source.prepareStatement(
                "CREATE TABLE IF NOT EXISTS " + ANNIConfig.getDBTablePrefix() + tableName +"("+args+")"
        )) {
            ps.execute();
        }
        catch (SQLException sqlex) {
            throw new RuntimeException(sqlex);
        }
    }

    private void reset(UUID player) {
        try {
            // clean up
            try (PreparedStatement ps = source.prepareStatement(
                    "DELETE FROM " + tableName("level") + " WHERE player = ?"
            )) {
                ps.setString(1, player.toString());
                ps.execute();
            }

            try (PreparedStatement ps = source.prepareStatement(
                    "DELETE FROM " + tableName("settings") + " WHERE player = ?"
            )) {
                ps.setString(1, player.toString());
                ps.execute();
            }

            try (PreparedStatement ps = source.prepareStatement(
                    "DELETE FROM " + tableName("available_kits") + " WHERE player = ?"
            )) {
                ps.setString(1, player.toString());
                ps.execute();
            }

            try (PreparedStatement ps = source.prepareStatement(
                    "DELETE FROM " + tableName("statistic") + " WHERE player = ?"
            )) {
                ps.setString(1, player.toString());
                ps.execute();
            }
        }
        catch (SQLException sqlex) {
            throw new RuntimeException(sqlex);
        }
    }

    @Override
    public void initPlayer(UUID player) {
        reset(player);
        try {
            try (PreparedStatement ps = source.prepareStatement(
                    "INSERT INTO "+ANNIConfig.getDBTablePrefix()+"level VALUES (?, ?, ?)"
            )) { // レベル
                ps.setString(1, player.toString());
                ps.setInt(2, 1);
                ps.setInt(3, CmnUtil.calcExp(2));
                ps.execute();
            }

            try (PreparedStatement ps = source.prepareStatement(
                    "INSERT INTO "+ANNIConfig.getDBTablePrefix()+"settings VALUES (?, ?)"
            )) { // 選択中のキット
                ps.setString(1, player.toString());
                ps.setString(2, "default");
                ps.execute();
            }

            try (PreparedStatement ps = source.prepareStatement(
                    "INSERT INTO " + tableName("available_kits") + " VALUES (?, ?)"
            )) { // 利用可能なキット
                ps.setString(1, player.toString());
                ps.setString(2, FileUtil.createGson().toJson(Collections.singletonList("default"), List.class));
                ps.execute();
            }

            try (PreparedStatement ps = source.prepareStatement(
                    "INSERT INTO " +tableName("statistic") + " VALUES (?, ?, ?, ?, ?, ?)"
            )) { // 統計
                ps.setString(1, player.toString());
                ps.setLong(2, 0);
                ps.setLong(3, 0);
                ps.setLong(4, 0);
                ps.setLong(5, 0);
                ps.setLong(6, 0);
                ps.execute();
            }
        }
        catch (SQLException sqlex) {
            throw new RuntimeException(sqlex);
        }
    }

    @Override
    public void initPlayerIfNotInitialized(UUID player) {
        try {
            // init level
            boolean isInitializedLevel;
            try (PreparedStatement ps = source.prepareStatement(
                    "SELECT * FROM " + tableName("level") + " WHERE player = ? LIMIT 1"
            )) {
                ps.setString(1, player.toString());
                ResultSet rs = ps.executeQuery();
                isInitializedLevel = rs.next();
            }

            if (!isInitializedLevel) {
                try (PreparedStatement ps = source.prepareStatement(
                        "INSERT INTO "+ tableName("level") + " VALUES (?, ?, ?)"
                )) {
                    ps.setString(1, player.toString());
                    ps.setInt(2, 1);
                    ps.setInt(3, CmnUtil.calcExp(2));
                    ps.execute();
                }
            }

            // init selected kit
            boolean isInitializedSettings;
            try (PreparedStatement ps = source.prepareStatement(
                    "SELECT * FROM " + tableName("settings") + " WHERE player = ? LIMIT 1"
            )) {
                ps.setString(1, player.toString());
                ResultSet rs = ps.executeQuery();
                isInitializedSettings = rs.next();
            }

            if (!isInitializedSettings) {
                try (PreparedStatement ps = source.prepareStatement(
                        "INSERT INTO "+ANNIConfig.getDBTablePrefix()+"settings VALUES (?, ?)"
                )) {
                    ps.setString(1, player.toString());
                    ps.setString(2, "default");
                    ps.execute();
                }
            }

            // init available kits
            boolean isInitializedAvailableKits;
            try (PreparedStatement ps = source.prepareStatement(
                    "SELECT * FROM " + ANNIConfig.getDBTablePrefix() + "available_kits WHERE player = ? LIMIT 1"
            )) {
                ps.setString(1, player.toString());
                ResultSet rs = ps.executeQuery();
                isInitializedAvailableKits = rs.next();
            }

            if (!isInitializedAvailableKits) {
                try (PreparedStatement ps = source.prepareStatement(
                        "INSERT INTO " + tableName("available_kits") + " VALUES (?, ?)"
                )) {
                    ps.setString(1, player.toString());
                    ps.setString(2, FileUtil.createGson().toJson(Collections.singletonList("default"), List.class));
                    ps.execute();
                }
            }

            // init statistic
            boolean isInitializedStatistic;
            try (PreparedStatement ps = source.prepareStatement(
                    "SELECT * FROM " + tableName("statistic") + " WHERE player = ? LIMIT 1"
            )) {
                ps.setString(1, player.toString());
                ResultSet rs = ps.executeQuery();
                isInitializedStatistic = rs.next();
            }

            if (!isInitializedStatistic) {
                try (PreparedStatement ps = source.prepareStatement(
                        "INSERT INTO " + tableName("statistic") + " VALUES (?, ?, ?, ?, ?, ?)"
                )) {
                    ps.setString(1, player.toString());
                    ps.setLong(2, 0);
                    ps.setLong(3, 0);
                    ps.setLong(4, 0);
                    ps.setLong(5, 0);
                    ps.setLong(6, 0);
                    ps.execute();
                }
            }
        }
        catch (SQLException sqlex) {
            throw new RuntimeException(sqlex);
        }
    }

    @Override
    public boolean restoreConnection() {
        closeConnection();
        try {
            connect();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean restoreConnectionIfClosed() {
        try {
            if (source == null || source.isClosed())
                return restoreConnection();
        }
        catch (SQLException sqlex) {
            throw new RuntimeException(sqlex);
        }

        return false;
    }

    @Override
    public boolean closeConnection() {
        try {
            if (source == null) return false;
            if (source.isClosed()) {
                source = null;
                return false;
            }

            source.close();
            source = null;
        }
        catch (SQLException sqlex) {
            throw new RuntimeException(sqlex);
        }

        return true;
    }

    @Override
    public int getLevel(UUID player) {
        try (PreparedStatement ps = source.prepareStatement(
                "SELECT level FROM " + tableName("level") + " WHERE player = ? LIMIT 1"
        )){
            ps.setString(1, player.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("level");
            }
        }
        catch (SQLException sqlex) {
            throw new RuntimeException(sqlex);
        }
        return 1;
    }

    @Override
    public void setLevel(UUID player, int level) {
        try (PreparedStatement ps = source.prepareStatement(
                "UPDATE "+tableName("level")+" SET level = ? WHERE player = ?"
        )) {
            ps.setInt(1, level);
            ps.setString(2, player.toString());
            ps.execute();
        }
        catch (SQLException sqlex) {
            throw new RuntimeException(sqlex);
        }
    }

    @Override
    public void addLevel(UUID player, int add) {
        try (PreparedStatement ps = source.prepareStatement(
                "UPDATE "+tableName("level")+" SET level + ? WHERE player = ?"
        )) {
            ps.setInt(1, add);
            ps.setString(2, player.toString());
            ps.execute();
        }
        catch (SQLException sqlex) {
            throw new RuntimeException(sqlex);
        }
    }

    @Override
    public void subtractLevel(UUID player, int subtract) {
        try (PreparedStatement ps = source.prepareStatement(
                "UPDATE "+tableName("level")+" SET level - ? WHERE player = ?"
        )) {
            ps.setInt(1, subtract);
            ps.setString(2, player.toString());
            ps.execute();
        }
        catch (SQLException sqlex) {
            throw new RuntimeException(sqlex);
        }
    }

    @Override
    public int getExp(UUID player) {
        try (PreparedStatement ps = source.prepareStatement(
                "SELECT level FROM " + tableName("level") + " WHERE player = ? LIMIT 1"
        )){
            ps.setString(1, player.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("exp");
            }
        }
        catch (SQLException sqlex) {
            throw new RuntimeException(sqlex);
        }
        return 0;
    }

    @Override
    public void setExp(UUID player, int exp) {
        try (PreparedStatement ps = source.prepareStatement(
                "UPDATE "+tableName("level")+" SET exp = ? WHERE player = ?"
        )) {
            ps.setInt(1, exp);
            ps.setString(2, player.toString());
            ps.execute();
        }
        catch (SQLException sqlex) {
            throw new RuntimeException(sqlex);
        }
    }

    @Override
    public void addExp(UUID player, int add) {
        try (PreparedStatement ps = source.prepareStatement(
                "UPDATE "+tableName("level")+" SET exp + ? WHERE player = ?"
        )) {
            ps.setInt(1, add);
            ps.setString(2, player.toString());
            ps.execute();
        }
        catch (SQLException sqlex) {
            throw new RuntimeException(sqlex);
        }
    }

    @Override
    public void subtractExp(UUID player, int subtract) {
        try (PreparedStatement ps = source.prepareStatement(
                "UPDATE "+tableName("level")+" SET exp - ? WHERE player = ?"
        )) {
            ps.setInt(1, subtract);
            ps.setString(2, player.toString());
            ps.execute();
        }
        catch (SQLException sqlex) {
            throw new RuntimeException(sqlex);
        }
    }

    @Override
    public AbstractKit getKit(UUID player) {
        try (PreparedStatement ps = source.prepareStatement(
                "SELECT kit FROM "+tableName("settings")+" WHERE player = ?"
        )) {
            ps.setString(1, player.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return ANNIKit.getAbsKitOrCustomById(rs.getString("kit"));
            }
        }
        catch (SQLException sqlex) {
            throw new RuntimeException(sqlex);
        }

        return ANNIKit.DEFAULT.getKit();
    }

    @Override
    public void setKit(UUID player, AbstractKit kit) {
        try (PreparedStatement ps = source.prepareStatement(
                "UPDATE " + tableName("settings") + " SET kit = ? WHERE player = ?"
        )) {
            ps.setString(1, kit.getId());
            ps.setString(2, player.toString());
            ps.execute();
        }
        catch (SQLException sqlex) {
            throw new RuntimeException(sqlex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getAvailableKits(UUID player) {
        try (PreparedStatement ps = source.prepareStatement(
                "SELECT kits FROM available_kits WHERE player = ?"
        )) {
            ps.setString(1, player.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return FileUtil.createGson().fromJson(rs.getString(1), List.class);
            }
        }
        catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }
        return Collections.singletonList("default");
    }

    @Override
    public void addAvailableKits(UUID player, ANNIKit kit) {
        List<String> list = getAvailableKits(player);
        list.add(kit.getKit().getId());


    }

    @Override
    public void removeAvailableKits(UUID player, String id) {

    }

    @Override
    public boolean isAvailableKit(UUID player, ANNIKit kit) {
        return false;
    }

    @Override
    public long getKillCount(UUID player) {
        return 0;
    }

    @Override
    public long setKillCount(UUID player, long kill) {
        return 0;
    }

    @Override
    public long addKillCount(UUID player, long add) {
        return 0;
    }

    @Override
    public long subtractKillCount(UUID player, long subtract) {
        return 0;
    }

    @Override
    public long getDeathCount(UUID player) {
        return 0;
    }

    @Override
    public long setDeathCount(UUID player, long death) {
        return 0;
    }

    @Override
    public long addDeathCount(UUID player, long add) {
        return 0;
    }

    @Override
    public long subtractDeathCount(UUID player, long subtract) {
        return 0;
    }

    @Override
    public long getCountDestroyedNexus(UUID player) {
        return 0;
    }

    @Override
    public long setCountDestroyedNexus(UUID player, long destroyed) {
        return 0;
    }

    @Override
    public long addCountDestroyedNexus(UUID player, long add) {
        return 0;
    }

    @Override
    public long subtractCountDestroyedNexus(UUID player, long subtract) {
        return 0;
    }

    @Override
    public long getWinCount(UUID player) {
        return 0;
    }

    @Override
    public long setWinCount(UUID player, long wins) {
        return 0;
    }

    @Override
    public long addWinCount(UUID player, long add) {
        return 0;
    }

    @Override
    public long subtractWinCount(UUID player, long subtract) {
        return 0;
    }

    @Override
    public long getLoseCount(UUID player) {
        return 0;
    }

    @Override
    public long setLoseCount(UUID player, long loses) {
        return 0;
    }

    @Override
    public long addLoseCount(UUID player, long add) {
        return 0;
    }

    @Override
    public long subtractLoseCount(UUID player, long subtract) {
        return 0;
    }

    private String tableName(String name) {
        return ANNIConfig.getDBTablePrefix();
    }
}
