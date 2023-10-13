package net.nekozouneko.anni.command.subcommand.admin;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.command.ASubCommand;
import net.nekozouneko.anni.map.ANNIMap;
import net.nekozouneko.anni.util.CmdUtil;
import net.nekozouneko.anni.util.CmnUtil;
import net.nekozouneko.anni.util.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CreateMapSubCommand extends ASubCommand {

    private final ANNIPlugin plugin = ANNIPlugin.getInstance();

    @Override
    public boolean execute(CommandSender sender, List<String> args) {
        if (args.size() < 2) {
            return false;
        }

        String id = args.get(0);
        if (id.startsWith("@")) {
            sender.sendMessage(plugin.getMessageManager().build("command.err.cant_use_char"));
            return true;
        }

        World world = Bukkit.getWorld(args.get(1));
        String display;

        if (args.size() >= 3) {
            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < args.size(); i++) {
                sb.append(args.get(i));
            }

            display = CmnUtil.replaceColorCode(sb.toString());
        }
        else display = id;

        if (world == null) {
            sender.sendMessage(plugin.getMessageManager()
                    .build("command.err.world_not_found", args.get(1))
            );
            return true;
        }

        ANNIMap map = new ANNIMap(id, world, display);
        File to = new File(new File(plugin.getDataFolder(), "maps"), id + ".json");

        if (FileUtil.writeGson(to, map, ANNIMap.class)) {
            plugin.getMapManager().reload();
            sender.sendMessage(plugin.getMessageManager().build("command.createmap.success"));
        }
        else {
            sender.sendMessage(plugin.getMessageManager().build("command.err.ioe"));
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, List<String> args) {
        if (args.size() == 2) {
            return CmdUtil.simpleTabComplete(
                    args.get(1),
                    Bukkit.getWorlds().stream()
                            .map(World::getName)
                            .collect(Collectors.toList())
            );
        }

        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        return "<id> <world> [<display>]";
    }

}
