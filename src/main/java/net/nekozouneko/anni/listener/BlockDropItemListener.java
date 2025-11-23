package net.nekozouneko.anni.listener;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.kit.ANNIKit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;

import java.util.ArrayList;

public class BlockDropItemListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockDrop(BlockDropItemEvent event) {
        var arena = ANNIPlugin.getInstance().getCurrentGame();

        if (!arena.getState().isInArena()) return;
        if (!BlockBreakListener.isLog(event.getBlock().getType())) return;

        if (ANNIKit.get(arena.getKit(event.getPlayer())) != ANNIKit.LUMBERJACK) return;

        new ArrayList<>(event.getItems()).forEach(item -> {
            if (!BlockBreakListener.isLog(item.getItemStack().getType())) return;

            item.setItemStack(item.getItemStack().add());
        });
    }
}
