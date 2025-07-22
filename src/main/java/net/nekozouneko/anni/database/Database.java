package net.nekozouneko.anni.database;

import java.util.UUID;

public interface Database {

    void close();

    void reset(UUID id, boolean force);

    void setLevel(UUID id, int level);

    int getLevel(UUID id);

    void addLevel(UUID id, int level);

    void setExp(UUID id, long exp);

    long getExp(UUID id);

    void addExp(UUID id, long add);

}
