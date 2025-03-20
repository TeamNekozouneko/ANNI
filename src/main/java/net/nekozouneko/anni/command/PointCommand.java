package net.nekozouneko.anni.command;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.message.MessageManager;
import net.nekozouneko.anni.util.VaultUtil;
import net.nekozouneko.commons.spigot.command.TabCompletes;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class PointCommand implements CommandExecutor, TabCompleter {

    private final MessageManager mm = ANNIPlugin.getInstance().getMessageManager();
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(mm.build("command.err.player_only"));
            }
            else {
                sender.sendMessage(mm.build("command.point.self_point", ANNIPlugin.getInstance().getPointManager().getPoint((Player)sender), mm.build("gui.shop.ext")));
            }
        }
        else {
            if (sender.hasPermission("anni.command.point.other")) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);

                if (VaultUtil.getEco().hasAccount(op)) {
                    sender.sendMessage(
                            mm.build(
                                    "command.point.other_point",
                                    op.getName(),
                                    VaultUtil.getEco().getBalance(op),
                                    mm.build("gui.shop.ext")
                            )
                    );
                }
                else {
                    sender.sendMessage("");
                }
            }
            else {
                sender.sendMessage(mm.build("command.err.no_perm"));
            }
        }

        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("anni.command.point.other")) {
            return TabCompletes.players(args[0], Bukkit.getOnlinePlayers());
        }

        return Collections.emptyList();
    }
}
