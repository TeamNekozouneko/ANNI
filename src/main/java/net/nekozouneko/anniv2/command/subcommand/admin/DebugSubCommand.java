package net.nekozouneko.anniv2.command.subcommand.admin;

import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.arena.ANNIArena;
import net.nekozouneko.anniv2.arena.ArenaState;
import net.nekozouneko.anniv2.arena.spectator.SpectatorManager;
import net.nekozouneko.anniv2.command.ASubCommand;
import net.nekozouneko.anniv2.kit.ANNIKit;
import net.nekozouneko.anniv2.kit.custom.CustomKit;
import net.nekozouneko.anniv2.kit.items.StunGrenade;
import net.nekozouneko.anniv2.listener.BlockBreakListener;
import net.nekozouneko.anniv2.util.CmdUtil;
import net.nekozouneko.commons.spigot.command.TabCompletes;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class DebugSubCommand extends ASubCommand {
    @Override
    public boolean execute(CommandSender sender, List<String> args) {
        ANNIArena ar = ANNIPlugin.getInstance().getCurrentGame();

        switch (args.get(0)) {
            case "set-state": {
                ArenaState stat = ArenaState.valueOf(args.get(1));
                if (stat.nextPhaseIn() > 0) ar.setTimer(stat.nextPhaseIn());
                ar.setState(stat);
                break;
            }
            case "set-timer": {
                ar.setTimer(Long.parseLong(args.get(1)));
                break;
            }
            case "clear-queued":
                BlockBreakListener.getQueuedOnDamageMap().remove(((Player) sender).getUniqueId());
                break;
            case "reload-map":
                ANNIPlugin.getInstance().getMapManager().reload();
                break;
            case "set-kit":
                ANNIPlugin.getInstance().getCurrentGame().setKit(Bukkit.getPlayer(args.get(1)), ANNIKit.getAbsKitOrCustomById(args.get(2)));
                break;
            case "get-stung":
                ((Player) sender).getInventory().addItem(StunGrenade.get(16));
                break;
            case "toggle-spec": {
                Player p;

                if (args.size() >= 2) {
                    p = Bukkit.getPlayer(args.get(1));
                }
                else p = (Player) sender;

                if (SpectatorManager.isSpectating(p)) {
                    SpectatorManager.remove(p);
                }
                else SpectatorManager.add(p);
                break;
            }
            case "watch-spec": {
                if (SpectatorManager.isWatchable((Player) sender)) {
                    SpectatorManager.removeWatchable((Player) sender);
                }
                else SpectatorManager.addWatchable((Player) sender);
                break;
            }
            default: return false;
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, List<String> args) {
        if (args.size() <= 1) {
            return CmdUtil.simpleTabComplete(args.get(0), "clear-queued", "get-stung", "reload-map", "set-kit", "set-state", "set-timer");
        }
        else {
            if (args.size() == 2) {
                switch (args.get(0)) {
                    case "set-state": {
                        return CmdUtil.simpleTabComplete(
                                args.get(1),
                                Arrays.stream(ArenaState.values())
                                        .map(ArenaState::name)
                                        .collect(Collectors.toList())
                        );
                    }
                    case "set-kit": {
                        Set<String> ids = new HashSet<>();
                        ids.addAll(Arrays.stream(ANNIKit.values())
                                .map((ak) -> ak.getKit().getId()).collect(Collectors.toList()));
                        ids.addAll(ANNIPlugin.getInstance().getCustomKitManager().getKits().stream()
                                .map(CustomKit::getId).collect(Collectors.toList()));

                        return TabCompletes.sorted(args.get(1), ids);
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        return "(clear-queued|get-stung|reload-map|set-kit|set-state|set-timer) [<args>]";
    }
}
