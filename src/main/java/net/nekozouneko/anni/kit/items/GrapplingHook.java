package net.nekozouneko.anni.kit.items;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.message.MessageManager;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.*;

public class GrapplingHook implements Listener {

    public static final List<EntityType> CANT_PULL_ENTITIES = Arrays.asList(
            EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.ENDER_CRYSTAL,
            EntityType.AREA_EFFECT_CLOUD, EntityType.WARDEN, EntityType.IRON_GOLEM
    );

    public static ItemStackBuilder builder() {
        MessageManager mm = ANNIPlugin.getInstance().getMessageManager();

        return ItemStackBuilder.of(Material.FISHING_ROD)
                .name(mm.build("item.grappling_hook.name"))
                .lore(mm.buildList("item.grappling_hook.lore"))
                .persistentData(new NamespacedKey(ANNIPlugin.getInstance(), "special-item"), PersistentDataType.STRING, "grappling-hook")
                .unbreakable(true)
                .itemFlags(ItemFlag.HIDE_UNBREAKABLE);
    }

    @EventHandler
    public void onHook(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.FISHING) {
            processRodIsGrapplingHook(event);
            return;
        }

        PersistentDataContainer c = event.getHook().getPersistentDataContainer();

        if (c.getOrDefault(new NamespacedKey(ANNIPlugin.getInstance(), "grappling-hook"), PersistentDataType.INTEGER, 0) != 1) return;

        switch (event.getState()) {
            case BITE:
            case REEL_IN: {
                event.setCancelled(true);
                break;
            }
            case IN_GROUND: {
                event.getPlayer().setVelocity(calculateVel(event.getPlayer().getLocation(), event.getHook().getLocation(), 2));
                break;
            }
            case CAUGHT_ENTITY: {
                if (CANT_PULL_ENTITIES.contains(event.getCaught().getType())) {
                    Location change = event.getCaught().getLocation().subtract(event.getPlayer().getLocation());
                    event.getPlayer().setVelocity(change.toVector().multiply(0.05));
                    event.getPlayer().playSound(event.getPlayer(), Sound.ENTITY_ITEM_BREAK, 1, 0);
                }
                else {
                    event.getCaught().setVelocity(calculateVel(event.getCaught().getLocation(), event.getPlayer().getLocation(), 1.5));
                }
                break;
            }
        }
    }

    private void processRodIsGrapplingHook(PlayerFishEvent event) {
        if (event.getHand() == null) return;

        ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
        if (item == null || item.getType().isAir()) return;

        PersistentDataContainer c = item.getItemMeta().getPersistentDataContainer();

        if (c.getOrDefault(new NamespacedKey(ANNIPlugin.getInstance(), "special-item"), PersistentDataType.STRING, "").equals("grappling-hook")) {
            boolean canUse = event.getPlayer().isVisualFire();

            try {
                canUse = canUse || event.getPlayer().isFrozen();
            }
            catch (Exception ignored) {}



            event.getHook().getPersistentDataContainer()
                    .set(new NamespacedKey(ANNIPlugin.getInstance(), "grappling-hook"), PersistentDataType.INTEGER, 1);
            event.getHook().setVelocity(event.getHook().getVelocity().multiply(1.5));
        }
    }

    private Vector calculateVel(Location from, Location to, double m) {
        Vector vel = to.subtract(from).toVector();

        return vel.normalize().multiply(m);
    }

}
