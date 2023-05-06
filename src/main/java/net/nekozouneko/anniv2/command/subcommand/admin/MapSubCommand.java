package net.nekozouneko.anniv2.command.subcommand.admin;

import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.arena.team.ANNITeam;
import net.nekozouneko.anniv2.command.ASubCommand;
import net.nekozouneko.anniv2.gui.map.MapEditor;
import net.nekozouneko.anniv2.gui.map.MapSelector;
import net.nekozouneko.anniv2.map.ANNIMap;
import net.nekozouneko.anniv2.map.Nexus;
import net.nekozouneko.anniv2.map.SpawnLocation;
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
                new MapSelector(ANNIPlugin.getInstance(), (Player) sender, 1, (map) -> {
                    if (map != null)
                        new MapEditor(plugin, (Player) sender, map).open();
                }).open();
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
            String defSpawn = map.getDefaultSpawn() != null ?
                    mem.yawPitchLocationFormat(map.getDefaultSpawn()) :
                    map.getBukkitWorld() != null ?
                            mem.yawPitchLocationFormat(map.getBukkitWorld().getSpawnLocation())
                                    + mem.build("command.map.info.3.using_world_spawn") :
                            mem.build("command.map.info.unset");


            sender.sendMessage(mem.buildLines(
                    "command.map.info",
                    map.getName(), map.getId(),
                    map.getWorld(),
                    map.canUseOnArena() ?
                            mem.build("command.map.info.2.avail") : mem.build("command.map.info.2.not_avail"),
                    defSpawn,
                    infoNexusLoc(map, ANNITeam.RED),
                    infoNexusLoc(map, ANNITeam.BLUE),
                    infoNexusLoc(map, ANNITeam.GREEN),
                    infoNexusLoc(map, ANNITeam.YELLOW),
                    infoSpawnLoc(map, ANNITeam.RED),
                    infoSpawnLoc(map, ANNITeam.BLUE),
                    infoSpawnLoc(map, ANNITeam.GREEN),
                    infoSpawnLoc(map, ANNITeam.YELLOW)
            ));
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
            return CmdUtil.simpleTabComplete(args.get(1), Arrays.asList("delete", "editor", "nexus", "spawn"));
        }
        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        return "[<map> (editor|nexus|spawn) [<args>]]";
    }

    private String infoNexusLoc(ANNIMap map, ANNITeam team) {
        Nexus nex = map.getNexus(team);
        return nex != null ? mem.blockLocationFormat(nex.getLocation()) : mem.build("command.map.info.unset");
    }

    private String infoSpawnLoc(ANNIMap map, ANNITeam team) {
        SpawnLocation sl = map.getSpawn(team);
        return sl != null ? mem.yawPitchLocationFormat(sl) : mem.build("command.map.info.unset");
    }

}
