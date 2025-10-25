package net.nekozouneko.anni.command.subcommand.admin;

import com.google.common.base.Enums;
import net.nekozouneko.anni.ANNIConfig;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.team.ANNITeam;
import net.nekozouneko.anni.command.ASubCommand;
import net.nekozouneko.anni.gui.arena.ArenaDashboard;
import net.nekozouneko.anni.map.ANNIMap;
import net.nekozouneko.anni.util.CmdUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ArenaSubCommand extends ASubCommand {

    private final ANNIPlugin plugin = ANNIPlugin.getInstance();

    @Override
    public boolean execute(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            if (sender instanceof Player) {
                new ArenaDashboard(plugin, (Player) sender).open();
                return true;
            }
            else return false;
        }

        var game = plugin.getCurrentGame();
        var teamManager = game.getTeamManager();
        var translation = plugin.getTranslationManager();
        Locale locale = sender instanceof Player player ? player.locale() : null;

        switch (args.get(0)) {
            case "disable-team": {
                if (teamManager.getTeams().size() <= 2) {
                    sender.sendMessage(translation.component(locale, "command.error.disable_team_limited"));
                    return true;
                }

                ANNITeam at = Enums.getIfPresent(ANNITeam.class, args.get(1)).orNull();
                if (at == null) sender.sendMessage(translation.component(locale, "command.error.team_undefined", args.get(1)));
                else {
                    teamManager.disable(at);
                    sender.sendMessage(translation.component(locale, "command.arena.disabled_team", at.getTeamName()));
                }
                break;
            }
            case "enable-team": {
                ANNITeam at = Enums.getIfPresent(ANNITeam.class, args.get(1)).orNull();
                if (at == null) sender.sendMessage(translation.component(locale, "command.error.team_undefined", args.get(1)));
                else {
                    teamManager.enable(at);
                    sender.sendMessage(translation.component(locale, "command.arena.enabled_team", at.getTeamName()));
                }
                break;
            }
            case "set-map": {
                if (args.get(1).equals("@vote")) {
                    plugin.getCurrentGame().setMap(null);
                    sender.sendMessage(translation.component(locale, "command.arena.map_random"));
                    return true;
                }

                ANNIMap map = plugin.getMapManager().getMap(args.get(1));
                if (map == null) {
                    sender.sendMessage(translation.component(locale, "command.error.map_not_found", args.get(1)));
                    return true;
                }

                if (!(plugin.getCurrentGame().getState().getId() >= 0)) {
                    plugin.getCurrentGame().setMap(map);
                    sender.sendMessage(translation.component(locale, "command.arena.set_map", args.get(1)));
                }
                break;
            }
            case "start": {
                if (plugin.getCurrentGame().start()) {
                    sender.sendMessage(translation.component(locale, "command.arena.starting"));
                }
                else {
                    sender.sendMessage(translation.component(locale, "command.error.game_start_failed"));
                }
                break;
            }
            case "move": {
                if (args.size() >= 2) {
                    ANNITeam at = Enums.getIfPresent(ANNITeam.class, args.get(1)).orNull();

                    if (at == null && !args.get(1).equals("@leave")) {
                        sender.sendMessage(translation.component(locale, "command.error.team_not_found", args.get(1)));
                        return true;
                    }

                    if (at != null && !teamManager.isEnabled(at)) {
                        sender.sendMessage(translation.component(locale, "command.error.disabled_team"));
                        return true;
                    }

                    Player target;
                    if (args.size() >= 3) {
                        target = Bukkit.getPlayer(args.get(2));

                        if (target == null) {
                            sender.sendMessage(translation.component(locale, "command.error.player_not_found", args.get(2)));
                            return true;
                        }
                    }
                    else {
                        if (!(sender instanceof Player)) {
                            sender.sendMessage(translation.component(locale, "command.error.player_only"));
                            return true;
                        }
                        target = (Player) sender;
                    }

                    // 既存チームから離脱させた上で新しいチームに参加させる
                    teamManager.leave(target.getUniqueId());
                    if (at != null) {
                        teamManager.join(at, target.getUniqueId());
                    }


                    if (at != null)
                        sender.sendMessage(translation.component(locale, "command.arena.move", target.getName(), at.getTeamName()));
                    else sender.sendMessage(translation.component(locale, "command.arena.move.leave", target.getName()));

                    if (game.getState().getId() >= 0) {
                        target.getInventory().clear();
                        target.getEnderChest().clear();
                        target.setHealth(0);
                        if (!target.equals(sender)) {
                            if (at != null)
                                target.sendMessage(translation.component(locale, "notify.moved", at.getTeamName()));
                            else sender.sendMessage(translation.component(locale, "notify.moved_leave"));
                        }
                    }
                }
                else return false;
                break;
            }
            case "sethealth": {
                if (args.size() >= 3) {
                    ANNITeam at = Enums.getIfPresent(ANNITeam.class, args.get(1).toUpperCase()).orNull();
                    if (at == null) return false;
                    int to = Integer.parseInt(args.get(2));

                    teamManager.getTeam(at).getNexus().setHealth(to);
                }
                else return false;
                break;
            }
            case "restore": {
                if (args.size() >= 2) {
                    ANNITeam at = Enums.getIfPresent(ANNITeam.class, args.get(1).toUpperCase()).orNull();
                    if (at == null) return false;

                    game.restoreNexus(at, ANNIConfig.getDefaultHealth());
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
            return CmdUtil.simpleTabComplete(args.get(0), "disable-team", "enable-team", "move", "sethealth", "set-map", "start", "restore");
        }
        if (args.size() == 2) {
            switch (args.get(0)) {
                case "disable-team":
                case "enable-team":
                case "move":
                case "sethealth":
                case "restore":
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
        return "[(disable-team|enable-team|move|set-map|start) [<args>]]";
    }
}
