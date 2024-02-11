package net.nekozouneko.anni.database;

import net.nekozouneko.anni.kit.ANNIKit;
import net.nekozouneko.anni.kit.AbstractKit;

import java.util.List;
import java.util.UUID;

public interface ANNIDatabase {

    boolean restoreConnection();

    boolean restoreConnectionIfClosed();

    boolean closeConnection();

    void initPlayer(UUID player);

    void initPlayerIfNotInitialized(UUID player);

    int getLevel(UUID player);

    void setLevel(UUID player, int level);

    void addLevel(UUID player, int add);

    void subtractLevel(UUID player, int subtract);

    int getExp(UUID player);

    void setExp(UUID player, int exp);

    void addExp(UUID player, int add);

    void subtractExp(UUID player, int subtract);

    AbstractKit getKit(UUID player);

    void setKit(UUID player, AbstractKit kit);

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

    long getWinCount(UUID player);

    long setWinCount(UUID player, long wins);

    long addWinCount(UUID player, long add);

    long subtractWinCount(UUID player, long subtract);

    long getLoseCount(UUID player);

    long setLoseCount(UUID player, long loses);

    long addLoseCount(UUID player, long add);

    long subtractLoseCount(UUID player, long subtract);

}
