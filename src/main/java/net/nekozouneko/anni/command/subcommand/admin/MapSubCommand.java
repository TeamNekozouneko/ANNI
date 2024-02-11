package net.nekozouneko.anni.command.subcommand.admin;

import com.google.common.base.Enums;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.team.ANNITeam;
import net.nekozouneko.anni.command.ASubCommand;
import net.nekozouneko.anni.gui.map.MapEditor;
import net.nekozouneko.anni.gui.map.MapSelector;
import net.nekozouneko.anni.listener.BlockBreakListener;
import net.nekozouneko.anni.map.ANNIMap;
import net.nekozouneko.anni.map.Nexus;
import net.nekozouneko.anni.map.SpawnLocation;
import net.nekozouneko.anni.message.MessageManager;
import net.nekozouneko.anni.util.CmdUtil;
import net.nekozouneko.anni.util.FileUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MapSubCommand extends ASubCommand {

    private final ANNIPlugin plugin = ANNIPlugin.getInstance();
    private final MessageManager mem = plugin.getMessageManager();

    @Override
    public boolean execute(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            if (sender instanceof Player) {
                new MapSelector(ANNIPlugin.getInstance(), (Player) sender, 1, false, (map) -> {
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

        // info command
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
                    infoSpawnLoc(map, ANNITeam.YELLOW),
                    infoTeamRegion(map, ANNITeam.RED),
                    infoTeamRegion(map, ANNITeam.BLUE),
                    infoTeamRegion(map, ANNITeam.GREEN),
                    infoTeamRegion(map, ANNITeam.YELLOW)
            ));
        }
        else { // other
            switch (args.get(1)) {
                case "defaultspawn": {
                    if (args.size() == 7) {
                        map.setDefaultSpawn(new SpawnLocation(
                                Double.parseDouble(args.get(2)),
                                Double.parseDouble(args.get(3)),
                                Double.parseDouble(args.get(4)),
                                Float.parseFloat(args.get(5)),
                                Float.parseFloat(args.get(6))
                        ));
                        FileUtil.writeGson(new File(plugin.getMapsDir(), map.getId() + ".json"), map, ANNIMap.class);
                        plugin.getMapManager().reload();
                    }
                    else if (args.size() == 5) {
                        map.setDefaultSpawn(new SpawnLocation(
                                Double.parseDouble(args.get(2)),
                                Double.parseDouble(args.get(3)),
                                Double.parseDouble(args.get(4))
                        ));
                        FileUtil.writeGson(new File(plugin.getMapsDir(), map.getId() + ".json"), map, ANNIMap.class);
                        plugin.getMapManager().reload();
                    }
                    else {
                        if (sender instanceof Player) {
                            map.setDefaultSpawn(((Player) sender).getLocation());
                            FileUtil.writeGson(new File(plugin.getMapsDir(), map.getId() + ".json"), map, ANNIMap.class);
                            plugin.getMapManager().reload();
                        }
                        else sender.sendMessage(mem.build("command.err.player_only"));
                    }
                    break;
                }
                case "delete": {
                    if (new File(plugin.getMapsDir(), map.getId() + ".json").delete()) {
                        plugin.getMapManager().reload();
                    }
                    else sender.sendMessage(mem.build("command.err.ioe"));
                    break;
                }
                case "editor": {
                    if (sender instanceof Player) {
                        new MapEditor(plugin, (Player) sender, map).open();
                    }
                    else sender.sendMessage(mem.build("command.err.player_only"));
                    break;
                }
                case "spawn": {
                    if (args.size() < 3) return false;
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(mem.build("command.err.player_only"));
                        return true;
                    }

                    map.setSpawn(
                            ANNITeam.valueOf(args.get(2)),
                            SpawnLocation.fromLocation(((Player) sender).getLocation()));
                    FileUtil.writeGson(new File(plugin.getMapsDir(), map.getId() + ".json"), map, ANNIMap.class);
                    plugin.getMapManager().reload();
                    break;
                }
                case "nexus": {
                    if (args.size() < 3) return false;
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(mem.build("command.err.player_only"));
                        return true;
                    }

                    BlockBreakListener.getQueuedOnDamageMap()
                            .put(((Player) sender).getUniqueId(), block -> {
                                        map.setNexus(ANNITeam.valueOf(args.get(2)), block.getLocation());
                                        FileUtil.writeGson(new File(plugin.getMapsDir(), map.getId() + ".json"), map, ANNIMap.class);
                                        plugin.getMapManager().reload();
                                    }
                            );
                    break;
                }
                case "region": {
                    if (args.size() < 4) return false;

                    ANNITeam at = Enums.getIfPresent(ANNITeam.class, args.get(2)).orNull();

                    if (at == null) {
                        sender.sendMessage(mem.build("command.err.team_undefined", args.get(2)));
                        return true;
                    }

                    String region = args.get(3);

                    ProtectedRegion pr = WorldGuard.getInstance().getPlatform().getRegionContainer()
                            .get(BukkitAdapter.adapt(map.getBukkitWorld()))
                            .getRegion(region);

                    if (pr == null) {
                        sender.sendMessage(mem.build("command.err.region_not_found", args.get(3)));
                        return true;
                    }

                    map.setTeamRegion(at, region);
                    FileUtil.writeGson(new File(plugin.getMapsDir(), map.getId() + ".json"), map, ANNIMap.class);
                    plugin.getMapManager().reload();
                    break;
                }
                default: return false;
            }
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, List<String> args) {
        if (args.size() <= 1) {
            return CmdUtil.simpleTabComplete(args.get(0),
                    plugin.getMapManager().getMaps().stream()
                            .map(ANNIMap::getId)
                            .collect(Collectors.toList())
            );
        }
        else if (args.size() == 2) {
            return CmdUtil.simpleTabComplete(args.get(1), "defaultspawn", "delete", "editor", "nexus", "region", "spawn");
        }
        else if (args.size() == 3) {
            if (args.get(1).equals("nexus") || args.get(1).equals("spawn") || args.get(1).equals("region")) {
                return CmdUtil.simpleTabComplete(
                        args.get(2),
                        Arrays.stream(ANNITeam.values())
                                .map(ANNITeam::name)
                                .collect(Collectors.toList())
                );
            }
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

    private String infoTeamRegion(ANNIMap map, ANNITeam team) {
        return map.getTeamRegion(team) == null ? mem.build("command.map.info.unset") : map.getTeamRegion(team);
    }

}
