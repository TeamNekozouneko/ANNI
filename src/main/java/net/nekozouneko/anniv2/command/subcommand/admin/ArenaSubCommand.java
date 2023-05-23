package net.nekozouneko.anniv2.command.subcommand.admin;

import com.google.common.base.Enums;
import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.arena.ANNIArena;
import net.nekozouneko.anniv2.arena.team.ANNITeam;
import net.nekozouneko.anniv2.command.ASubCommand;
import net.nekozouneko.anniv2.map.ANNIMap;
import net.nekozouneko.anniv2.message.MessageManager;
import net.nekozouneko.anniv2.util.CmdUtil;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ArenaSubCommand extends ASubCommand {

    private final ANNIPlugin plugin = ANNIPlugin.getInstance();
    private final MessageManager mm = plugin.getMessageManager();

    @Override
    public boolean execute(CommandSender sender, List<String> args) {
        if (args.size() == 0) {
            return false;
        }

        ANNIArena current = plugin.getCurrentGame();

        switch (args.get(0)) {
            case "disable-team": {
                if (current.getTeams().size() <= 2) {
                    sender.sendMessage(mm.build("command.err.no_more_teams_can_be_disabled"));
                    return true;
                }

                ANNITeam at = Enums.getIfPresent(ANNITeam.class, args.get(1)).orNull();
                if (at == null) sender.sendMessage(mm.build("command.err.team_undefined"), args.get(1));
                else {
                    current.disableTeam(at);
                    sender.sendMessage(mm.build("command.arena.disabled_team", at.getTeamName()));
                }
                break;
            }
            case "enable-team": {
                ANNITeam at = Enums.getIfPresent(ANNITeam.class, args.get(1)).orNull();
                if (at == null) sender.sendMessage(mm.build("command.err.team_undefined"), args.get(1));
                else {
                    current.enableTeam(at);
                    sender.sendMessage(mm.build("command.arena.enabled_team", at.getTeamName()));
                }
                break;
            }
            case "set-map": {
                ANNIMap map = plugin.getMapManager().getMap(args.get(1));
                if (map == null) {
                    sender.sendMessage(mm.build("command.err.map_not_found", args.get(1)));
                    return true;
                }

                if (!(plugin.getCurrentGame().getState().getId() >= 0)) {
                    plugin.getCurrentGame().setMap(map);
                }
                break;
            }
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, List<String> args) {
        if (args.size() == 1) {
            return CmdUtil.simpleTabComplete(args.get(0), "disable-team", "enable-team", "set-map");
        }
        if (args.size() == 2) {
            switch (args.get(0)) {
                case "disable-team":
                case "enable-team":
                    return CmdUtil.simpleTabComplete(
                            args.get(1),
                            Arrays.stream(ANNITeam.values())
                                    .map(ANNITeam::name)
                                    .collect(Collectors.toList())
                    );
                case "set-map":
                    return CmdUtil.simpleTabComplete(
                            args.get(1),
                            plugin.getMapManager().getMaps().stream()
                                    .map(ANNIMap::getId)
                                    .collect(Collectors.toList())
                    );
            }
        }

        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        return "(disable-team|enable-team) [<args>]";
    }
}
