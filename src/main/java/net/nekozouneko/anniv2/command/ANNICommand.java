package net.nekozouneko.anniv2.command;

import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.command.subcommand.anni.RuleSubCommand;
import net.nekozouneko.anniv2.command.subcommand.anni.VersionSubCommand;
import net.nekozouneko.anniv2.util.CmdUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

public class ANNICommand implements CommandExecutor, TabCompleter {

    private final ANNIPlugin plugin = ANNIPlugin.getInstance();

    private final ASubCommand versionSubCommand = new VersionSubCommand();
    private final ASubCommand ruleSubCommand = new RuleSubCommand();

    private final Map<String, ASubCommand> subcommands = new HashMap<String, ASubCommand>() {
        {
            put("info", versionSubCommand);
            put("manual", ruleSubCommand);
            put("rule", ruleSubCommand);
            put("ver", versionSubCommand);
            put("version", versionSubCommand);
        }
    };
    private final List<String> doNotTabComp = Arrays.asList("info", "manual", "ver");

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
            //TODO あとで言語化する
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
            subCommands.removeAll(doNotTabComp);
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
