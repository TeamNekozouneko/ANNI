package net.nekozouneko.anni.command;

import net.nekozouneko.anni.ANNIConfig;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.message.MessageManager;
import net.nekozouneko.anni.point.LevelManager;
import net.nekozouneko.anni.util.CmnUtil;
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

public class PlayerCommand implements CommandExecutor, TabCompleter {

    private final MessageManager mm = ANNIPlugin.getInstance().getMessageManager();
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(mm.build("command.err.player_only"));
            }
            else {
                ANNIPlugin plugin = ANNIPlugin.getInstance();
                int lvl = plugin.getLevelManager().getLevel(player);
                long exp = plugin.getLevelManager().getExp(player);

                double progress = (double) exp / LevelManager.calculateExpForNextLevel(lvl);

                sender.sendMessage(ANNIPlugin.getInstance().getTranslationManager().componentLines(player, "command.player.result",
                        sender.getName(), lvl, exp, LevelManager.calculateExpForNextLevel(lvl),
                        CmnUtil.progressBar(progress, 20),
                        String.format("%,d", plugin.getPointManager().getPoint(player))
                ));
            }
        }
        else {
            if (sender.hasPermission("anni.command.player.other")) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);

                if (VaultUtil.getEco().hasAccount(op)) {
                    ANNIPlugin plugin = ANNIPlugin.getInstance();
                    int lvl = plugin.getLevelManager().getLevel(op.getUniqueId());
                    long exp = plugin.getLevelManager().getExp(op.getUniqueId());

                    double progress = (double) exp / LevelManager.calculateExpForNextLevel(lvl);

                    sender.sendMessage(ANNIPlugin.getInstance().getTranslationManager().componentLines(sender instanceof Player ? ((Player) sender).locale() : ANNIConfig.getDefaultLocale(), "command.player.result",
                            op.getName(), lvl, exp, LevelManager.calculateExpForNextLevel(lvl),
                            CmnUtil.progressBar(progress, 20),
                            String.format("%,d", plugin.getPointManager().getPoint(op))
                    ));
                }
                else {
                    sender.sendMessage("-");
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
        if (sender.hasPermission("anni.command.player.other")) {
            return TabCompletes.players(args[0], Bukkit.getOnlinePlayers());
        }

        return Collections.emptyList();
    }
}
