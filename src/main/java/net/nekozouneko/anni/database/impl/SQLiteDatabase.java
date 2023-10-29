package net.nekozouneko.anni.database.impl;

import net.nekozouneko.anni.ANNIConfig;
import net.nekozouneko.anni.database.ANNIDatabase;
import net.nekozouneko.anni.kit.ANNIKit;
import net.nekozouneko.anni.kit.AbsANNIKit;
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
        createTable("level", "player TEXT NOT NULL, level integer, exp integer");
        createTable("settings", "player TEXT NOT NULL, kit TEXT");
        createTable("available_kits", "player TEXT NOT NULL, kits TEXT NOT NULL DEFAULT '[]'");
        createTable("statistic", "player TEXT NOT NULL, kills integer, deaths integer, nexus integer, wins integer, loses integer");
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

    // 現状データベースは更新しないためコメントアウト
    /*private List<String> getColumns(String table) {
        List<String> columns = new ArrayList<>();
        try (PreparedStatement ps = source.prepareStatement(
                "SELECT * FROM " + table + " LIMIT 0"
        )) {
            for (int i = 0; i < cs.getMetaData().getColumnCount(); i++) {
                columns.add(cs.getMetaData().getColumnName(i));
            }
        }
        catch (SQLException sqlex) {
            throw new RuntimeException(sqlex);
        }

        return columns;
    }

    private void appendColumnIfNotContains(String table, String name, String modifiers) {
        if (!getColumns(table).contains(name)) {
            try (PreparedStatement ps = source.prepareStatement(
                    "ALTER TABLE "+ table +" ADD COLUMN " + name + " " + modifiers
            )) {
                ps.execute();
            }
            catch (SQLException sqlex) {
                throw new RuntimeException(sqlex);
            }
        }
    }*/

    private void reset(UUID player) {
        try {
            // clean up
            try (PreparedStatement ps = source.prepareStatement(
                    "DELETE FROM level WHERE player = ?"
            )) {
                ps.setString(1, player.toString());
                ps.execute();
            }

            try (PreparedStatement ps = source.prepareStatement(
                    "DELETE FROM settings WHERE player = ?"
            )) {
                ps.setString(1, player.toString());
                ps.execute();
            }

            try (PreparedStatement ps = source.prepareStatement(
                    "DELETE FROM available_kits WHERE player = ?"
            )) {
                ps.setString(1, player.toString());
                ps.execute();
            }

            try (PreparedStatement ps = source.prepareStatement(
                    "DELETE FROM statistic WHERE player = ?"
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
                    "INSERT INTO "+ANNIConfig.getDBTablePrefix()+"available_kits VALUES (?, ?)"
            )) { // 利用可能なキット
                ps.setString(1, player.toString());
                ps.setString(2, FileUtil.createGson().toJson(Collections.singletonList("default"), List.class));
                ps.execute();
            }

            try (PreparedStatement ps = source.prepareStatement(
                    "INSERT INTO "+ANNIConfig.getDBTablePrefix()+"statistic VALUES (?, ?, ?, ?, ?, ?)"
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
                    "SELECT * FROM " + ANNIConfig.getDBTablePrefix() + "level WHERE player = ? LIMIT 1"
            )) {
                ps.setString(1, player.toString());
                ResultSet rs = ps.executeQuery();
                isInitializedLevel = rs.next();
            }

            if (!isInitializedLevel) {
                try (PreparedStatement ps = source.prepareStatement(
                        "INSERT INTO "+ANNIConfig.getDBTablePrefix()+"level VALUES (?, ?, ?)"
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
                    "SELECT * FROM " + ANNIConfig.getDBTablePrefix() + "settings WHERE player = ? LIMIT 1"
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
                        "INSERT INTO "+ANNIConfig.getDBTablePrefix()+"available_kits VALUES (?, ?)"
                )) {
                    ps.setString(1, player.toString());
                    ps.setString(2, FileUtil.createGson().toJson(Collections.singletonList("default"), List.class));
                    ps.execute();
                }
            }

            // init statistic
            boolean isInitializedStatistic;
            try (PreparedStatement ps = source.prepareStatement(
                    "SELECT * FROM " + ANNIConfig.getDBTablePrefix() + "statistic WHERE player = ? LIMIT 1"
            )) {
                ps.setString(1, player.toString());
                ResultSet rs = ps.executeQuery();
                isInitializedStatistic = rs.next();
            }

            if (!isInitializedStatistic) {
                try (PreparedStatement ps = source.prepareStatement(
                        "INSERT INTO "+ANNIConfig.getDBTablePrefix()+"statistic VALUES (?, ?, ?, ?, ?, ?)"
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
                "SELECT level FROM " + ANNIConfig.getDBTablePrefix() + "level WHERE player = ? LIMIT 1"
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
                "UPDATE "+ANNIConfig.getDBTablePrefix()+"level SET level = ? WHERE player = ?"
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
                "UPDATE "+ANNIConfig.getDBTablePrefix()+"level SET level + ? WHERE player = ?"
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
                "UPDATE "+ANNIConfig.getDBTablePrefix()+"level SET level - ? WHERE player = ?"
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
                "SELECT level FROM " + ANNIConfig.getDBTablePrefix() + "level WHERE player = ? LIMIT 1"
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
                "UPDATE "+ANNIConfig.getDBTablePrefix()+"level SET exp = ? WHERE player = ?"
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
                "UPDATE "+ANNIConfig.getDBTablePrefix()+"level SET exp + ? WHERE player = ?"
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
                "UPDATE "+ANNIConfig.getDBTablePrefix()+"level SET exp - ? WHERE player = ?"
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
    public AbsANNIKit getKit(UUID player) {
        try (PreparedStatement ps = source.prepareStatement(
                "SELECT kit FROM "+ANNIConfig.getDBTablePrefix()+"settings WHERE player = ?"
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
    public void setKit(UUID player, AbsANNIKit kit) {
        try (PreparedStatement ps = source.prepareStatement(
                "UPDATE " + ANNIConfig.getDBTablePrefix()+"settings SET kit = ? WHERE player = ?"
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
    public List<String> getAvailableKits(UUID player) {
        return null;
    }

    @Override
    public void addAvailableKits(UUID player, ANNIKit kit) {

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
}
