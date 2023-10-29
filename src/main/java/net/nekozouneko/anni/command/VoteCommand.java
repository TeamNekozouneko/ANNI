package net.nekozouneko.anni.command;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.message.MessageManager;
import net.nekozouneko.anni.util.CmdUtil;
import net.nekozouneko.anni.vote.VoteManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class VoteCommand implements CommandExecutor, TabCompleter {

    private final ANNIPlugin plugin = ANNIPlugin.getInstance();
    private final MessageManager mm = plugin.getMessageManager();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(mm.build("command.err.player_only"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(mm.build("command.usage", cmd.getUsage()));
            return true;
        }

        if (VoteManager.isNowVoting(plugin.getCurrentGame().getId())) {
            if (VoteManager.hasChoices(plugin.getCurrentGame().getId())) {
                if (!VoteManager.getChoices(plugin.getCurrentGame().getId()).contains(args[0])) {
                    sender.sendMessage(mm.build("command.err.invalid_vote", args[0]));
                    return true;
                }

                VoteManager.vote(plugin.getCurrentGame().getId(), (OfflinePlayer) sender, args[0]);
                sender.sendMessage(mm.build("command.vote.voted", args[0]));
            }
        }
        else {
            sender.sendMessage(mm.build("command.err.cant_vote_now"));
        }

        return true;
    }

    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1 && VoteManager.hasChoices(plugin.getCurrentGame().getId())) {
            return CmdUtil.simpleTabComplete(
                    args[0],
                    VoteManager.getChoices(plugin.getCurrentGame().getId()).stream()
                            .filter(obj -> obj instanceof String)
                            .map(obj -> (String) obj)
                            .collect(Collectors.toSet())
            );
        }
        return Collections.emptyList();
    }
}
