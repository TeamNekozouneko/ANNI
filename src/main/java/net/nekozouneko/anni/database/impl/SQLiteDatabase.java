package net.nekozouneko.anni.database.impl;

import net.nekozouneko.anni.database.Database;

import java.sql.*;
import java.util.UUID;

public class SQLiteDatabase implements Database {

    private final Connection connection;

    public SQLiteDatabase(String path) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);

        connection.prepareStatement("CREATE TABLE IF NOT EXISTS level (id STRING PRIMARY KEY, level INTEGER DEFAULT 1, exp INTEGER DEFAULT 0)")
                .execute();
    }

    @Override
    public void close() {
        try {
            if (!connection.isClosed())
                connection.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reset(UUID id, boolean force) {
        try {
            if (force) {
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM level WHERE id = ?")) {
                    statement.setString(1, id.toString());
                    statement.execute();
                }
            }

            try (PreparedStatement statement = connection.prepareStatement("INSERT OR IGNORE INTO level (id) VALUES (?)")) {
                statement.setString(1, id.toString());
                statement.execute();
            }
        }
        catch (SQLException sql) {
            sql.printStackTrace();
        }
    }

    @Override
    public void setLevel(UUID id, int level) {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE level SET level = ? WHERE id = ?")) {
            statement.setInt(1, level);
            statement.setString(2, id.toString());
            statement.executeUpdate();
        }
        catch (SQLException sql) {
            sql.printStackTrace();
        }
    }

    @Override
    public int getLevel(UUID id) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM level WHERE id = ?")) {
            statement.setString(1, id.toString());
            try (ResultSet set = statement.executeQuery()) {
                if (!set.next()) return 1;
                return set.getInt("level");
            }
        }
        catch (SQLException sql) {
            sql.printStackTrace();
        }

        return 1;
    }

    @Override
    public void addLevel(UUID id, int level) {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE level SET level = level + ? WHERE id = ?")) {
            statement.setLong(1, level);
            statement.setString(2, id.toString());
            statement.executeUpdate();
        }
        catch (SQLException sql) {
            sql.printStackTrace();
        }
    }

    @Override
    public void setExp(UUID id, long exp) {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE level SET exp = ? WHERE id = ?")) {
            statement.setLong(1, exp);
            statement.setString(2, id.toString());
            statement.executeUpdate();
        }
        catch (SQLException sql) {
            sql.printStackTrace();
        }
    }

    @Override
    public long getExp(UUID id) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM level WHERE id = ?")) {
            statement.setString(1, id.toString());
            try (ResultSet set = statement.executeQuery()) {
                return set.getLong("exp");
            }
        }
        catch (SQLException sql) {
            sql.printStackTrace();
        }

        return 0;
    }

    @Override
    public void addExp(UUID id, long exp) {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE level SET exp = exp + ? WHERE id = ?")) {
            statement.setLong(1, exp);
            statement.setString(2, id.toString());
            statement.executeUpdate();
        }
        catch (SQLException sql) {
            sql.printStackTrace();
        }
    }
}
