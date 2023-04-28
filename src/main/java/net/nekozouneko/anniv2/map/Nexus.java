package net.nekozouneko.anniv2.map;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;

import java.util.Random;

public class Nexus {

    public static void destroyEffects(Location loc) {
        World w = loc.getWorld();

        w.spawnParticle(Particle.LAVA, loc, 100, .75, .75, .75, 0);
        w.playSound(loc, Sound.BLOCK_ANVIL_PLACE, 1.5f, (new Random().nextInt(5) / 10f));
    }

    public static void finalDestroyEffects(Location loc) {
        destroyEffects(loc);

        World w = loc.getWorld();

        w.spawnParticle(Particle.EXPLOSION_HUGE, 10, 1, 1, 1, 0);
        w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0);
    }

    private final BlockVector3 location;

    public Nexus(Location loc) {
        this.location = BukkitAdapter.asBlockVector(loc);
    }

    public Nexus(BlockVector3 loc) {
        this.location = loc;
    }

    public Location asBukkitLocation(World world) {
        return new Location(world, location.getX(), location.getY(), location.getZ());
    }

    public BlockVector3 getLocation() {
        return BlockVector3.at(location.getX(), location.getY(), location.getZ());
    }

}
