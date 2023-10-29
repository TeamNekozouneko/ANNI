package net.nekozouneko.anni.command.subcommand.anni;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.command.ASubCommand;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class RuleSubCommand extends ASubCommand {

    @Override
    public boolean execute(CommandSender sender, List<String> args) {
        sender.sendMessage(ANNIPlugin.getInstance().getMessageManager()
                .buildLines("command.rule")
        );
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        return "";
    }
}
