package net.nekozouneko.anni.command;

import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class ASubCommand {

    public abstract boolean execute(CommandSender sender, List<String> args);

    public abstract List<String> tabComplete(CommandSender sender, List<String> args);

    public abstract String getUsage();

}
