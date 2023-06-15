package net.nekozouneko.anniv2.database.abs;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.nekozouneko.anniv2.ANNIConfig;
import net.nekozouneko.anniv2.database.ANNIDatabase;
import net.nekozouneko.anniv2.kit.ANNIKit;

import java.io.File;
import java.sql.CallableStatement;
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
    public int getLevel() {
        return 0;
    }

    @Override
    public void setLevel(int level) {

    }

    @Override
    public void addLevel(int add) {

    }

    @Override
    public void subtractLevel(int subtract) {

    }

    @Override
    public int getExp() {
        return 0;
    }

    @Override
    public void setExp(int exp) {

    }

    @Override
    public void addExp(int add) {

    }

    @Override
    public void subtractExp(int subtract) {

    }

    @Override
    public ANNIKit getKit() {
        return null;
    }

    @Override
    public void setKit(ANNIKit kit) {

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
}
