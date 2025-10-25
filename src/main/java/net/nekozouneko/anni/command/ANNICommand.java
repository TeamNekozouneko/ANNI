package net.nekozouneko.anni.command;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.command.subcommand.anni.RuleSubCommand;
import net.nekozouneko.anni.util.CmdUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class ANNICommand implements CommandExecutor, TabCompleter {

    private final ANNIPlugin plugin = ANNIPlugin.getInstance();

    private final ASubCommand ruleSubCommand = new RuleSubCommand();

    private final Map<String, ASubCommand> subcommands = new HashMap<>() {
        {
            put("manual", ruleSubCommand);
            put("rule", ruleSubCommand);
        }
    };
    private final List<String> doNotTabComp = Arrays.asList("manual");

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        var translation = plugin.getTranslationManager();
        Locale locale = sender instanceof Player player ? player.locale() : null;

        if (args.length > 0) {
            ASubCommand sc = subcommands.get(args[0]);

            if (sc != null) {
                if (!sc.execute(sender, Arrays.asList(args).subList(1, args.length))) {
                    sender.sendMessage(translation.component(
                            locale,
                            "command.usage",
                            "/" + label + " " +args[0] + " " + sc.getUsage()
                    ));
                }
            }
            else {
                sender.sendMessage(
                        translation.component(
                                locale,
                                "command.error.subcommand_not_found", args[0]
                        )
                );
            }
        }
        else {
            sender.sendMessage(translation.component(
                    locale,
                    "command.usage",
                    cmd.getUsage().replace("<command>", label)
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
