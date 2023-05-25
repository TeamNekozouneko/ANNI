package net.nekozouneko.anniv2.command;

import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.command.subcommand.admin.*;
import net.nekozouneko.anniv2.util.CmdUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

public class ANNIAdminCommand implements CommandExecutor, TabCompleter {

    private final ANNIPlugin plugin = ANNIPlugin.getInstance();

    private final ASubCommand debugSubCommand = new DebugSubCommand();
    private final ASubCommand mapSubCommand = new MapSubCommand();
    private final ASubCommand createMapSubCommand = new CreateMapSubCommand();
    private final ASubCommand setLobbySubCommand = new SetLobbySubCommand();
    private final ASubCommand arenaSubCommand = new ArenaSubCommand();

    private final Map<String, ASubCommand> subcommands = new HashMap<String, ASubCommand>() {
        {
            put("debug", debugSubCommand);
            put("map", mapSubCommand);
            put("create-map", createMapSubCommand);
            put("set-lobby", setLobbySubCommand);
            put("arena", arenaSubCommand);
        }
    };

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0) {
            ASubCommand sc = subcommands.get(args[0]);

            if (sc != null) {
                if (!sc.execute(sender, Arrays.asList(args).subList(1, args.length))) {
                    sender.sendMessage(plugin.getMessageManager().build(
                            "command.usage",
                            "&c/" + label + " " +args[0] + " " + sc.getUsage()
                    ));
                }
            }
            else {
                sender.sendMessage(
                        plugin.getMessageManager().build(
                                "command.err.subcommand_not_found", args[0]
                        )
                );
            }
        }
        else {
            sender.sendMessage(plugin.getMessageManager().build(
                    "command.usage",
                    "§c" + cmd.getUsage()
            ));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getPermission() != null && !sender.hasPermission(cmd.getPermission())) return Collections.emptyList();
        if (args.length <= 1) {
            List<String> subCommands = new ArrayList<>(subcommands.keySet());
            Collections.sort(subCommands);
            return CmdUtil.simpleTabComplete(args[0], subCommands);
        }
        else {
            ASubCommand sc = subcommands.get(args[0]);

            return sc != null ?
                    sc.tabComplete(sender, Arrays.asList(args).subList(1, args.length)) :
                    Collections.emptyList();
        }
    }
}
