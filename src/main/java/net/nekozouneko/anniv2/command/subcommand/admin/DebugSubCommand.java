package net.nekozouneko.anniv2.command.subcommand.admin;

import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.arena.ArenaManager;
import net.nekozouneko.anniv2.arena.ArenaState;
import net.nekozouneko.anniv2.arena.team.ANNITeam;
import net.nekozouneko.anniv2.command.ASubCommand;
import net.nekozouneko.anniv2.util.CmdUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DebugSubCommand extends ASubCommand {
    @Override
    public void execute(CommandSender sender, List<String> args) {
        ArenaManager am = ANNIPlugin.getInstance().getArenaManager();

        switch (args.get(0)) {
            case "set-state": {
                am.getArenaByPlayer((Player) sender).setState(ArenaState.valueOf(args.get(1)));
                break;
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, List<String> args) {
        if (args.size() <= 1) {
            return CmdUtil.simpleTabComplete(args.get(0), "set-state");
        }
        else {
            switch (args.get(0)) {
                case "set-state": {
                    return CmdUtil.simpleTabComplete(
                            args.get(1),
                            Arrays.stream(ArenaState.values())
                                    .map(ArenaState::name)
                                    .collect(Collectors.toList())
                    );
                }
                default:
                    return Collections.emptyList();
            }
        }
    }

    @Override
    public String getUsage(Command cmd, String label) {
        return "/" + label + " set-state <State>";
    }
}
