package net.nekozouneko.anni.map;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.math.BlockVector3;
import lombok.Getter;
import net.nekozouneko.anni.arena.team.ANNITeam;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class ANNIMap {

    @Getter
    private final String id;
    @Getter
    private String name;

    @Getter
    private final String world;

    private final EnumMap<ANNITeam, Nexus> nexus = new EnumMap<>(ANNITeam.class);
    private final EnumMap<ANNITeam, SpawnLocation> spawn = new EnumMap<>(ANNITeam.class);

    @Getter
    private SpawnLocation defaultSpawn;

    public ANNIMap(String id, String world, String name) {
        this.id = id;
        this.world = world;
        this.name = name;
    }

    public ANNIMap(String id, World world, String name) {
        this.id = id;
        this.world = world.getName();
        this.name = name;
    }

    public boolean canUseOnArena() {
        return (
                getBukkitWorld() != null
                && nexus.size() >= 4
                && spawn.size() >= 4
        );
    }

    public World getBukkitWorld() {
        return Bukkit.getWorld(world);
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

    public void setNexus(ANNITeam team, Nexus nexus) {
        this.nexus.put(team, nexus);
    }

    public void setNexus(ANNITeam team, Location location) {
        nexus.put(team, new Nexus(location));
    }

    public void setNexus(ANNITeam team, BlockVector3 vector3) {
        nexus.put(team, new Nexus(vector3));
    }

    public void setSpawn(ANNITeam team, SpawnLocation slocation) {
        spawn.put(team, slocation);
    }
}
