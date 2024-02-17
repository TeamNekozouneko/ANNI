package net.nekozouneko.anni.command;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.spectator.SpectatorManager;
import net.nekozouneko.anni.gui.shop.CombatShop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CombatShopCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(
                    ANNIPlugin.getInstance().getMessageManager().build("command.err.player_only")
            );
            return true;
        }

        if (SpectatorManager.isSpectating((Player) sender)) return true;

        if (ANNIPlugin.getInstance().getCurrentGame().getState().getId() >= 0) {
            new CombatShop(ANNIPlugin.getInstance(), (Player) sender).open();
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return Collections.emptyList();
    }
}
