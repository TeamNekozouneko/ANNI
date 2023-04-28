package net.nekozouneko.anniv2.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class ASubCommand {

    public abstract void execute(CommandSender sender, List<String> args);

    public abstract List<String> tabComplete(CommandSender sender, List<String> args);

    public abstract String getUsage(Command cmd, String label);

}
