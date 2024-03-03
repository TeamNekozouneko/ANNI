package net.nekozouneko.anni.listener;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.ANNIArena;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

public class BlockPistonListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onExtendPiston(BlockPistonExtendEvent event) {
        ANNIArena game = ANNIPlugin.getInstance().getCurrentGame();

        if (game.getCopyWorld() == null || event.getBlock().getWorld().equals(game.getCopyWorld())) return;

        boolean cancel = false;

        for (Block block : event.getBlocks()) {
            if (BlockBreakListener.getRegenerativeBlocks().contains(block.getType())) {
                cancel = true;
                break;
            }
        }

        if (cancel) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onRetractPiston(BlockPistonRetractEvent event) {
        ANNIArena game = ANNIPlugin.getInstance().getCurrentGame();

        if (game.getCopyWorld() == null || event.getBlock().getWorld().equals(game.getCopyWorld())) return;

        boolean cancel = false;

        for (Block block : event.getBlocks()) {
            if (BlockBreakListener.getRegenerativeBlocks().contains(block.getType())) {
                cancel = true;
                break;
            }
        }

        if (cancel) event.setCancelled(true);
    }

}
