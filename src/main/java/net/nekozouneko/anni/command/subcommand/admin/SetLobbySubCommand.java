package net.nekozouneko.anni.command.subcommand.admin;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.command.ASubCommand;
import net.nekozouneko.anni.util.CmdUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SetLobbySubCommand extends ASubCommand {
    @Override
    public boolean execute(CommandSender sender, List<String> args) {
        if (args.size() == 6) {
            ANNIPlugin.getInstance().setLobby(
                    new Location(
                            Bukkit.getWorld(args.get(0)),
                            Double.parseDouble(args.get(1)),
                            Double.parseDouble(args.get(2)),
                            Double.parseDouble(args.get(3)),
                            Float.parseFloat(args.get(4)),
                            Float.parseFloat(args.get(5))
                    )
            );
        }
        else if (args.size() == 4) {
            ANNIPlugin.getInstance().setLobby(
                    new Location(
                            Bukkit.getWorld(args.get(0)),
                            Double.parseDouble(args.get(1)),
                            Double.parseDouble(args.get(2)),
                            Double.parseDouble(args.get(3))
                    )
            );
        }
        else if (args.size() == 0) {
            ANNIPlugin.getInstance().setLobby(((Player) sender).getLocation());
        }
        else return false;

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, List<String> args) {
        if (args.size() == 1)
            return CmdUtil.simpleTabComplete(args.get(0),
                    Bukkit.getWorlds().stream()
                            .map(World::getName)
                            .collect(Collectors.toList())
        );
        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        return "[<world> <x> <y> <z> [<yaw>] [<pitch>]]";
    }
}
