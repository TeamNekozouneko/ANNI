package net.nekozouneko.anniv2.command.subcommand.admin;

import com.google.common.base.Enums;
import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.arena.ANNIArena;
import net.nekozouneko.anniv2.arena.team.ANNITeam;
import net.nekozouneko.anniv2.command.ASubCommand;
import net.nekozouneko.anniv2.map.ANNIMap;
import net.nekozouneko.anniv2.message.MessageManager;
import net.nekozouneko.anniv2.util.CmdUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
                if (args.get(1).equals("@vote")) {
                    plugin.getCurrentGame().setMap(null);
                    sender.sendMessage(mm.build("command.arena.map_random"));
                    return true;
                }

                ANNIMap map = plugin.getMapManager().getMap(args.get(1));
                if (map == null) {
                    sender.sendMessage(mm.build("command.err.map_not_found", args.get(1)));
                    return true;
                }

                if (!(plugin.getCurrentGame().getState().getId() >= 0)) {
                    plugin.getCurrentGame().setMap(map);
                    sender.sendMessage(mm.build("command.arena.set_map", args.get(1)));
                }
                break;
            }
            case "start": {
                if (plugin.getCurrentGame().start()) {
                    sender.sendMessage(mm.build("command.arena.starting"));
                }
                else {
                    sender.sendMessage(mm.build("command.err.game_start_failed"));
                }
                break;
            }
            case "move": {
                if (args.size() >= 2) {
                    ANNITeam at = Enums.getIfPresent(ANNITeam.class, args.get(1)).orNull();

                    if (at == null && !args.get(1).equals("@leave")) {
                        sender.sendMessage(mm.build("command.err.team_not_found", args.get(1)));
                        return true;
                    }

                    if (!current.isEnabledTeam(at)) {
                        sender.sendMessage(mm.build("command.err.disabled_team"));
                        return true;
                    }

                    Player target;
                    if (args.size() >= 3) {
                        target = Bukkit.getPlayer(args.get(2));

                        if (target == null) {
                            sender.sendMessage(mm.build("command.err.player_not_found", args.get(2)));
                            return true;
                        }
                    }
                    else {
                        if (!(sender instanceof Player)) {
                            sender.sendMessage(mm.build("command.err.player_only"));
                            return true;
                        }
                        target = (Player) sender;
                    }

                    current.setTeam(target, at);
                    if (at != null)
                        sender.sendMessage(mm.build("command.arena.move", target.getName(), at.getTeamName()));
                    else sender.sendMessage(mm.build("command.arena.move.leave", target.getName()));

                    if (current.getState().getId() >= 0) {
                        target.getInventory().clear();
                        target.getEnderChest().clear();
                        target.setHealth(0);
                        if (!target.equals(sender)) {
                            if (at != null)
                                target.sendMessage(mm.build("notify.moved", at.getTeamName()));
                            else sender.sendMessage(mm.build("notify.moved_leave"));
                        }
                    }
                }
                else return false;
                break;
            }
            default: return false;
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, List<String> args) {
        if (args.size() == 1) {
            return CmdUtil.simpleTabComplete(args.get(0), "disable-team", "enable-team", "move", "set-map", "start");
        }
        if (args.size() == 2) {
            switch (args.get(0)) {
                case "disable-team":
                case "enable-team":
                case "move":
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
        if (args.size() == 3) {
            switch (args.get(0)) {
                case "move":
                    return CmdUtil.simpleTabComplete(
                            args.get(2),
                            Bukkit.getOnlinePlayers().stream()
                                    .map(Player::getName)
                                    .collect(Collectors.toList())
                    );
            }
        }

        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        return "(disable-team|enable-team|move|set-map|start) [<args>]";
    }
}
