package net.nekozouneko.anni.item;

import com.google.common.base.Enums;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.ANNIArena;
import net.nekozouneko.anni.arena.team.ANNITeam;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public class NexusCompass implements Listener {

    public static ItemStackBuilder builder() {
        return ItemStackBuilder.of(Material.COMPASS)
                .name(ANNIPlugin.getInstance().getMessageManager().build("item.nexus_compass.name"));
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (e.getItem() == null || e.getItem().getType() != Material.COMPASS) return;
        if (!(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;

        PersistentDataContainer c = e.getItem().getItemMeta().getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(ANNIPlugin.getInstance(), "nexus-target");
        List<ANNITeam> enabled = new ArrayList<>(
                ANNIPlugin.getInstance().getCurrentGame().getTeams().keySet()
        );

        if (c.has(key, PersistentDataType.STRING)) {
            ANNITeam at = Enums.getIfPresent(ANNITeam.class, c.getOrDefault(key, PersistentDataType.STRING, "")).orNull();
            if (at == null || !enabled.contains(at) || enabled.size()-1 == enabled.lastIndexOf(at)) {
                setTarget(e.getPlayer(), e.getItem(), enabled.get(0));
                return;
            }

            List<ANNITeam> tmp = new ArrayList<>(enabled);
            while (tmp.contains(at)) {
                tmp.remove(0);
            }

            if (tmp.isEmpty()) {
                setTarget(e.getPlayer(), e.getItem(), enabled.get(0));
                return;
            }

            setTarget(e.getPlayer(), e.getItem(), tmp.get(0));
        }
        else {
            setTarget(e.getPlayer(), e.getItem(), enabled.get(0));
        }
    }

    private void setTarget(Player player, ItemStack is, ANNITeam target) {
        ItemMeta meta = is.getItemMeta();

        Team t = ANNIPlugin.getInstance().getCurrentGame().getTeam(target);
        meta.setDisplayName(ANNIPlugin.getInstance().getMessageManager().build(
                "item.nexus_compass.name.target",
                t.getColor() + t.getDisplayName()
        ));

        PersistentDataContainer c = meta.getPersistentDataContainer();
        c.set(new NamespacedKey(ANNIPlugin.getInstance(), "nexus-target"), PersistentDataType.STRING, target.name());

        is.setItemMeta(meta);

        ANNIArena arena = ANNIPlugin.getInstance().getCurrentGame();
        player.setCompassTarget(
                arena.getMap().getNexus(target).asBukkitLocation(arena.getCopyWorld())
        );
    }
}
