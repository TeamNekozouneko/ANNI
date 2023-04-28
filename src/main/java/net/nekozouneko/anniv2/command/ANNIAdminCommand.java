package net.nekozouneko.anniv2.command;

import net.nekozouneko.anniv2.command.subcommand.admin.DebugSubCommand;
import net.nekozouneko.anniv2.util.CmdUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

public class ANNIAdminCommand implements CommandExecutor, TabCompleter {

    private final ASubCommand debugSubCommand = new DebugSubCommand();

    private final Map<String, ASubCommand> subcommands = new HashMap<String, ASubCommand>() {
        {
            put("debug", debugSubCommand);
        }
    };

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length <= 1) {
            sender.sendMessage(cmd.getUsage());
        }
        else {
            ASubCommand sc = subcommands.get(args[0]);

            if (sc != null) sc.execute(sender, Arrays.asList(args).subList(1, args.length));
            else {
                // error message
                sender.sendMessage("ERR");
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length <= 1) {
            return CmdUtil.simpleTabComplete(args[0], subcommands.keySet());
        }
        else {
            ASubCommand sc = subcommands.get(args[0]);

            return sc != null ?
                    sc.tabComplete(sender, Arrays.asList(args).subList(1, args.length)) :
                    Collections.emptyList();
        }
    }
}
