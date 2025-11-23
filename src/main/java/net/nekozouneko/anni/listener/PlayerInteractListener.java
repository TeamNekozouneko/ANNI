package net.nekozouneko.anni.listener;

import lombok.RequiredArgsConstructor;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.ANNIArena;
import net.nekozouneko.anni.kit.ANNIKit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class PlayerInteractListener implements Listener {

    private final ANNIArena arena;

    private static final List<Material> AXES = Arrays.asList(Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE);

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.useItemInHand() == Event.Result.DENY) return;

        if (!event.getAction().isRightClick()) return;

        if (!arena.getState().isInArena()) return;

        if (ANNIKit.get(arena.getKit(event.getPlayer())) != ANNIKit.LUMBERJACK) return;

        if (event.getItem() == null || !AXES.contains(event.getItem().getType())) return;

        if (event.getPlayer().getCooldown(event.getItem().getType()) > 0) return;

        event.setCancelled(true);

        Snowball throwing = event.getPlayer().launchProjectile(Snowball.class);
        throwing.setItem(event.getItem().clone());
        throwing.getPersistentDataContainer().set(new NamespacedKey(ANNIPlugin.getInstance(), "special-item"), PersistentDataType.STRING, "axes");

        event.getItem().subtract();

        AXES.forEach(axe -> event.getPlayer().setCooldown(axe, 5 * 20));
    }

}
