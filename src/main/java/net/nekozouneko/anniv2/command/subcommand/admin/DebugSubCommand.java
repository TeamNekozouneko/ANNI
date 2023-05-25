package net.nekozouneko.anniv2.command.subcommand.admin;

import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.arena.ANNIArena;
import net.nekozouneko.anniv2.arena.ArenaState;
import net.nekozouneko.anniv2.command.ASubCommand;
import net.nekozouneko.anniv2.gui.shop.CombatShop;
import net.nekozouneko.anniv2.gui.shop.PotionShop;
import net.nekozouneko.anniv2.kit.ANNIKit;
import net.nekozouneko.anniv2.kit.items.StunGrenade;
import net.nekozouneko.anniv2.listener.BlockBreakListener;
import net.nekozouneko.anniv2.util.CmdUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
                ANNIPlugin.getInstance().getCurrentGame().setKit(Bukkit.getPlayer(args.get(1)), ANNIKit.getKitById(args.get(2)));
                break;
            case "get-stung":
                ((Player) sender).getInventory().addItem(StunGrenade.get(16));
                break;
            case "potion":
                new PotionShop(ANNIPlugin.getInstance(), (Player) sender).open();
                break;
            case "combat":
                new CombatShop(ANNIPlugin.getInstance(), (Player) sender).open();
                break;
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, List<String> args) {
        if (args.size() <= 1) {
            return CmdUtil.simpleTabComplete(args.get(0), "clear-queued", "get-stung", "reload-map", "set-kit", "set-state", "set-timer");
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
    public String getUsage() {
        return "set-state <state>";
    }
}
