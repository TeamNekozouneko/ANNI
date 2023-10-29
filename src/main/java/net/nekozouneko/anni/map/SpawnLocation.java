package net.nekozouneko.anni.map;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;

@Getter @Setter
public final class SpawnLocation implements Cloneable {

    public static SpawnLocation fromLocation(Location loc) {
        return new SpawnLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    private double x, y, z;
    private float yaw, pitch;

    public SpawnLocation(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = 0;
        this.pitch = 0;
    }

    public SpawnLocation(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public SpawnLocation(long x, long y, long z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = 0;
        this.pitch = 0;
    }

    public SpawnLocation(long x, long y, long z, int yaw, int pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public SpawnLocation clone() {
        try {
            return (SpawnLocation) super.clone();
        }
        catch (CloneNotSupportedException ignored) {}

        return null;
    }


    public Location toLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }
}
