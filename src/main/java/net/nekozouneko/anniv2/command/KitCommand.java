package net.nekozouneko.anniv2.command;

import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.gui.kit.KitSelector;
import net.nekozouneko.anniv2.listener.PlayerDamageListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class KitCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            if (PlayerDamageListener.isFighting((Player) sender)) {
                sender.sendMessage(ANNIPlugin.getInstance().getMessageManager()
                        .build("command.err.now_fighting")
                );
            }
            else new KitSelector(ANNIPlugin.getInstance(), (Player) sender, 1).open();
        }
        else sender.sendMessage(ANNIPlugin.getInstance().getMessageManager().build("command.err.player_only"));

        return true;
    }

    
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return Collections.emptyList();
    }
}
