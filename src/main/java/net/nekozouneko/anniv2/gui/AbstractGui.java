package net.nekozouneko.anniv2.gui;

import net.nekozouneko.anniv2.ANNIPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.RegisteredListener;

import java.util.Arrays;

public abstract class AbstractGui implements InventoryHolder, Listener {

    protected final ANNIPlugin plugin;
    protected final Player player;
    protected Inventory inventory;

    public AbstractGui(ANNIPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        unregisterAllGuiListeners(player);
        update();
        player.openInventory(inventory);
    }

    public abstract void update();

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static void unregisterAllGuiListeners(Player player) {
        HandlerList.getHandlerLists().forEach((hl) -> {
            Arrays.stream(hl.getRegisteredListeners())
                    .map(RegisteredListener::getListener)
                    .filter(listener -> listener instanceof AbstractGui)
                    .forEach(HandlerList::unregisterAll);
        });
    }

}
