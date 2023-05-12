package net.nekozouneko.anniv2.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.arena.ANNIArena;
import net.nekozouneko.anniv2.arena.team.ANNITeam;
import net.nekozouneko.anniv2.map.Nexus;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class BlockBreakListener implements Listener {

    private static final Map<UUID, Consumer<Block>> queuedOnDamage = new HashMap<>();

    public static Map<UUID, Consumer<Block>> getQueuedOnDamageMap() {
        return queuedOnDamage;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBreak(BlockBreakEvent e) {
        Consumer<Block> cb = queuedOnDamage.get(e.getPlayer().getUniqueId());
        if (cb != null) {
            cb.accept(e.getBlock());
            queuedOnDamage.remove(e.getPlayer().getUniqueId());
            e.setCancelled(true);
            return;
        }

        ANNIPlugin plugin = ANNIPlugin.getInstance();
        ANNIArena current = plugin.getCurrentGame();

        if (current.getState().canDestroyNexus()) {
            for (Map.Entry<ANNITeam, Nexus> ent : current.getMap().getNexuses().entrySet()) {
                Location loc = BukkitAdapter.adapt(
                        current.getCopyWorld(),
                        ent.getValue().getLocation()
                );
                if (e.getBlock().getLocation().equals(loc)) {
                    current.damageNexusHealth(ent.getKey(), 1, e.getPlayer());
                    if (current.isNexusLost(ent.getKey())) {
                        Nexus.finalDestroyEffects(loc);
                        Bukkit.getScheduler().runTask(plugin, () -> e.getBlock().setType(Material.BEDROCK));
                    }
                    else {
                        Nexus.destroyEffects(loc);
                        Bukkit.getScheduler().runTask(plugin, () -> e.getBlock().setType(Material.END_STONE));
                    }
                    break;
                }
            }
        }
    }

}
