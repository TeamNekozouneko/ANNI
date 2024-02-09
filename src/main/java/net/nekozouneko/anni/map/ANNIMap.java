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

    private EnumMap<ANNITeam, Nexus> nexus = new EnumMap<>(ANNITeam.class);
    private EnumMap<ANNITeam, SpawnLocation> spawn = new EnumMap<>(ANNITeam.class);
    private EnumMap<ANNITeam, String> team_region = new EnumMap<>(ANNITeam.class);

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
                && team_region.size() >= 4
        );
    }

    public World getBukkitWorld() {
        return Bukkit.getWorld(world);
    }

    public String getTeamRegion(ANNITeam team) {
        if (team_region == null) team_region = new EnumMap<>(ANNITeam.class);
        return team_region.get(team);
    }

    public Map<ANNITeam, String> getTeamRegions() {
        if (team_region == null) team_region = new EnumMap<>(ANNITeam.class);
        return Collections.unmodifiableMap(team_region);
    }

    public void setTeamRegion(ANNITeam team, String region) {
        if (team_region == null) team_region = new EnumMap<>(ANNITeam.class);
        team_region.put(team, region);
    }

    public Nexus getNexus(ANNITeam team) {
        if (nexus == null) nexus = new EnumMap<>(ANNITeam.class);
        return nexus.get(team);
    }

    public Map<ANNITeam, Nexus> getNexuses() {
        if (nexus == null) nexus = new EnumMap<>(ANNITeam.class);
        return Collections.unmodifiableMap(nexus);
    }

    public SpawnLocation getSpawn(ANNITeam team) {
        Preconditions.checkNotNull(team);

        if (spawn == null) spawn = new EnumMap<>(ANNITeam.class);

        return spawn.get(team);
    }

    public SpawnLocation getSpawnOrDefault(ANNITeam team) {
        if (spawn == null) spawn = new EnumMap<>(ANNITeam.class);
        return spawn.getOrDefault(team, defaultSpawn);
    }

    public Map<ANNITeam, SpawnLocation> getSpawns() {
        if (spawn == null) spawn = new EnumMap<>(ANNITeam.class);
        return Collections.unmodifiableMap(spawn);
    }

    public void setDefaultSpawn(Location location) {
        defaultSpawn = SpawnLocation.fromLocation(location);
    }

    @SuppressWarnings("LombokSetterMayBeUsed")
    public void setDefaultSpawn(SpawnLocation location) {
        defaultSpawn = location;
    }

    public void setNexus(ANNITeam team, Nexus nexus) {
        if (this.nexus == null) this.nexus = new EnumMap<>(ANNITeam.class);
        this.nexus.put(team, nexus);
    }

    public void setNexus(ANNITeam team, Location location) {
        if (nexus == null) nexus = new EnumMap<>(ANNITeam.class);
        nexus.put(team, new Nexus(location));
    }

    public void setNexus(ANNITeam team, BlockVector3 vector3) {
        if (nexus == null) nexus = new EnumMap<>(ANNITeam.class);
        nexus.put(team, new Nexus(vector3));
    }

    public void setSpawn(ANNITeam team, SpawnLocation slocation) {
        if (spawn == null) spawn = new EnumMap<>(ANNITeam.class);
        spawn.put(team, slocation);
    }
}
