package net.nekozouneko.anniv2.command.subcommand.admin;

import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.command.ASubCommand;
import net.nekozouneko.anniv2.gui.map.MapSelector;
import net.nekozouneko.anniv2.map.ANNIMap;
import net.nekozouneko.anniv2.message.MessageManager;
import net.nekozouneko.anniv2.util.CmdUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MapSubCommand extends ASubCommand {

    private final ANNIPlugin plugin = ANNIPlugin.getInstance();
    private final MessageManager mem = plugin.getMessageManager();

    @Override
    public boolean execute(CommandSender sender, List<String> args) {
        if (args.size() == 0) {
            if (sender instanceof Player) {
                new MapSelector(ANNIPlugin.getInstance(), (Player) sender, 1, null).open();
            }

            return true;
        }

        ANNIMap map = ANNIPlugin.getInstance().getMapManager()
                .getMap(args.get(0));

        if (map == null) {
            sender.sendMessage(mem.build("command.err.map_not_found", args.get(0)));
            return true;
        }

        if (args.size() == 1) {

        }
        else {

        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, List<String> args) {
        if (args.size() <= 1) {
            return CmdUtil.simpleTabComplete(args.get(0),
                    ANNIPlugin.getInstance().getMapManager().getMaps().stream()
                            .map(ANNIMap::getId)
                            .collect(Collectors.toList())
            );
        }
        else if (args.size() == 2) {
            return CmdUtil.simpleTabComplete(args.get(1), Arrays.asList("editor", "nexus", "spawn"));
        }
        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        return "map (editor|nexus|spawn) [<args>]";
    }

}
