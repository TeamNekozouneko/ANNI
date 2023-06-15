package net.nekozouneko.anniv2.database;

import com.zaxxer.hikari.HikariDataSource;
import net.nekozouneko.anniv2.kit.ANNIKit;

import java.util.List;
import java.util.UUID;

public interface ANNIDatabase {

    boolean restoreConnection();

    boolean restoreConnectionIfClosed();

    boolean closeConnection();

    HikariDataSource getSource();

    int getLevel();

    void setLevel(int level);

    void addLevel(int add);

    void subtractLevel(int subtract);

    int getExp();

    void setExp(int exp);

    void addExp(int add);

    void subtractExp(int subtract);

    ANNIKit getKit();

    void setKit(ANNIKit kit);

    List<String> getAvailableKits(UUID player);

    void addAvailableKits(UUID player, ANNIKit kit);

    void removeAvailableKits(UUID player, String id);

    default void removeAvailableKits(UUID player, ANNIKit kit) {
        removeAvailableKits(player, kit.getKit().getId());
    }

    boolean isAvailableKit(UUID player, ANNIKit kit);

    long getKillCount(UUID player);

    long setKillCount(UUID player, long kill);

    long addKillCount(UUID player, long add);

    long subtractKillCount(UUID player, long subtract);

    long getDeathCount(UUID player);

    long setDeathCount(UUID player, long death);

    long addDeathCount(UUID player, long add);

    long subtractDeathCount(UUID player, long subtract);

    long getCountDestroyedNexus(UUID player);

    long setCountDestroyedNexus(UUID player, long destroyed);

    long addCountDestroyedNexus(UUID player, long add);

    long subtractCountDestroyedNexus(UUID player, long subtract);

}
