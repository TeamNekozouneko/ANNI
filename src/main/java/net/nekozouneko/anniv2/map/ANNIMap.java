package net.nekozouneko.anniv2.map;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.math.BlockVector3;
import net.nekozouneko.anniv2.arena.team.ANNITeam;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class ANNIMap {

    private final String id;
    private String name;

    private final String world;

    private final EnumMap<ANNITeam, Nexus> nexus = new EnumMap<>(ANNITeam.class);
    private final EnumMap<ANNITeam, SpawnLocation> spawn = new EnumMap<>(ANNITeam.class);

    private SpawnLocation defaultSpawn;

    public ANNIMap(String id, String name, String world) {
        this.id = id;
        this.name = name;
        this.world = world;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getWorld() {
        return world;
    }

    public World getBukkitWorld() {
        return Bukkit.getWorld(world);
    }

    public SpawnLocation getDefaultSpawn() {
        return defaultSpawn;
    }

    public Nexus getNexus(ANNITeam team) {
        return nexus.get(team);
    }

    public Map<ANNITeam, Nexus> getNexuses() {
        return Collections.unmodifiableMap(nexus);
    }

    public SpawnLocation getSpawn(ANNITeam team) {
        Preconditions.checkNotNull(team);

        return spawn.get(team);
    }

    public SpawnLocation getSpawnOrDefault(ANNITeam team) {
        return spawn.getOrDefault(team, defaultSpawn);
    }

    public Map<ANNITeam, SpawnLocation> getSpawns() {
        return Collections.unmodifiableMap(spawn);
    }

    public void setDefaultSpawn(Location location) {
        defaultSpawn = SpawnLocation.fromLocation(location);
    }

    public void setDefaultSpawn(SpawnLocation location) {
        defaultSpawn = location;
    }
}
