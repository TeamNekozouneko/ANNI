package net.nekozouneko.anniv2.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.nekozouneko.anniv2.ANNIConfig;
import net.nekozouneko.anniv2.database.ANNIDatabase;
import net.nekozouneko.anniv2.kit.ANNIKit;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLiteDatabase implements ANNIDatabase {

    private HikariDataSource source;

    public SQLiteDatabase() {
        new File(ANNIConfig.getLocalDBPath()).mkdirs();

        connect();
        createTable("level", "player TEXT NOT NULL, level integer, exp integer");
        createTable("selected_kit", "player TEXT NOT NULL, kit TEXT");
        createTable("available_kits", "player TEXT NOT NULL, kits TEXT NOT NULL DEFAULT '[]'");
        createTable("statistic", "player TEXT NOT NULL, kills integer, deaths integer, nexus integer, wins integer, loses integer");
    }

    private void connect() {
        HikariConfig conf = new HikariConfig();

        conf.setDriverClassName("org.sqlite.JDBC");
        conf.setJdbcUrl("jdbc:sqlite:"+ANNIConfig.getLocalDBPath());
        conf.setAutoCommit(true);

        source = new HikariDataSource(conf);
    }

    private void createTable(String tableName, String args) {
        try (CallableStatement cs = source.getConnection().prepareCall(
                "CREATE TABLE IF NOT EXISTS " + ANNIConfig.getDBTablePrefix() + tableName +"("+args+")"
        )) {
            cs.execute();
        }
        catch (SQLException sqlex) {
            throw new RuntimeException(sqlex);
        }
    }

    private List<String> getColumns(String table) {
        List<String> columns = new ArrayList<>();
        try (CallableStatement cs = source.getConnection().prepareCall(
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
            try (CallableStatement cs = source.getConnection().prepareCall(
                    "ALTER TABLE "+ table +" ADD COULMN " + name + " " + modifiers
            )) {
                cs.execute();
            }
            catch (SQLException sqlex) {
                throw new RuntimeException(sqlex);
            }
        }
    }

    @Override
    public void initPlayer(UUID player) {
        try {
            // clean up
            try (CallableStatement cs = source.getConnection().prepareCall(
                    "DELETE FROM level WHERE player = ?"
            )) {
                cs.setString(1, player.toString());
                cs.execute();
            }

            try (CallableStatement cs = source.getConnection().prepareCall(
                    "DELETE FROM selected_kit WHERE player = ?"
            )) {
                cs.setString(1, player.toString());
                cs.execute();
            }

            try (CallableStatement cs = source.getConnection().prepareCall(
                    "DELETE FROM available_kits WHERE player = ?"
            )) {
                cs.setString(1, player.toString());
                cs.execute();
            }

            try (CallableStatement cs = source.getConnection().prepareCall(
                    "DELETE FROM statistic WHERE player = ?"
            )) {
                cs.setString(1, player.toString());
                cs.execute();
            }
        }
        catch (SQLException sqlex) {
            throw new RuntimeException(sqlex);
        }
    }

    @Override
    public void initPlayerIfNotInitialized(UUID player) {

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
        if (source == null || source.isClosed())
            return restoreConnection();

        return false;
    }

    @Override
    public boolean closeConnection() {
        if (source.isClosed()) return false;

        source.close();
        source = null;

        return true;
    }

    @Override
    public HikariDataSource getSource() {
        return source;
    }

    @Override
    public int getLevel(UUID player) {
        try (CallableStatement cs = source.getConnection().prepareCall(
                "SELECT level FROM " + ANNIConfig.getDBTablePrefix() + "level WHERE player = ? LIMIT 1"
        )){
            cs.setString(1, player.toString());
            ResultSet rs = cs.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        catch (SQLException sqlex) {
            throw new RuntimeException(sqlex);
        }
        return 1;
    }

    @Override
    public void setLevel(UUID player, int level) {

    }

    @Override
    public void addLevel(UUID player, int add) {

    }

    @Override
    public void subtractLevel(UUID player, int subtract) {

    }

    @Override
    public int getExp(UUID player) {
        return 0;
    }

    @Override
    public void setExp(UUID player, int exp) {

    }

    @Override
    public void addExp(UUID player, int add) {

    }

    @Override
    public void subtractExp(UUID player, int subtract) {

    }

    @Override
    public ANNIKit getKit(UUID player) {
        return null;
    }

    @Override
    public void setKit(UUID player, ANNIKit kit) {

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
