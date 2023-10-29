package net.nekozouneko.anni.command.subcommand.admin;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.command.ASubCommand;
import net.nekozouneko.anni.gui.kit.CustomKitEditor;
import net.nekozouneko.anni.kit.custom.CustomKit;
import net.nekozouneko.anni.util.FileUtil;
import net.nekozouneko.commons.spigot.command.TabCompletes;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class KitSubCommand extends ASubCommand {

    private final ANNIPlugin plugin = ANNIPlugin.getInstance();

    @Override
    public boolean execute(CommandSender sender, List<String> args) {
        if (args.size() < 2) return false;

        CustomKit ck = plugin.getCustomKitManager().getKit(args.get(0));

        if (ck == null) {
            sender.sendMessage(plugin.getMessageManager().build("command.err.kit_not_found", args.get(0)));
            return true;
        }

        switch (args.get(1)) {
            case "editor": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.getMessageManager().build("command.err.player_only"));
                    return true;
                }
                new CustomKitEditor(plugin, (Player) sender, ck).open();
                break;
            }
            case "name": {
                if (args.size() == 2) {
                    sender.sendMessage(plugin.getMessageManager().build("command.kit.current_name", ck.getName()));
                    return true;
                }

                ck.setName(String.join(" ", args.subList(2, args.size())));
                saveAndReload(ck);
                sender.sendMessage(plugin.getMessageManager().build("command.kit.set_name", String.join(" ", args.subList(2, args.size()))));
                break;
            }
            case "shortname": {
                if (args.size() == 2) {
                    sender.sendMessage(plugin.getMessageManager().build("command.kit.current_shortname", ck.getShortName()));
                    return true;
                }

                ck.setShortName(args.get(2));
                saveAndReload(ck);
                sender.sendMessage(plugin.getMessageManager().build("command.kit.set_shortname", args.get(2)));
                break;
            }
            case "icon": {
                if (args.size() == 2) {
                    sender.sendMessage(plugin.getMessageManager().build("command.kit.current_icon", ck.getIcon().name()));
                    return true;
                }

                Optional<Material> opt = Enums.getIfPresent(Material.class, args.get(2).toUpperCase());
                if (opt.isPresent()) {
                    ck.setIcon(opt.get());
                    saveAndReload(ck);
                    sender.sendMessage(plugin.getMessageManager().build("command.kit.set_icon", opt.get().name()));
                }
                else {
                    sender.sendMessage(plugin.getMessageManager().build("command.err.material_not_defined", args.get(2).toUpperCase()));
                }
                break;
            }
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, List<String> args) {
        if (args.size() == 1) {
            return TabCompletes.sorted(args.get(0), plugin.getCustomKitManager().getKits().stream()
                    .map(CustomKit::getId)
                    .collect(Collectors.toList())
            );
        }
        else {
            CustomKit k = plugin.getCustomKitManager().getKit(args.get(0));

            if (args.size() == 2) {
                return TabCompletes.sorted(args.get(1), "editor", "icon", "name", "shortname");
            }
            else if (args.size() == 3) {
                if (args.get(1).equals("icon")) {
                    return TabCompletes.sorted(args.get(2),
                            Arrays.stream(Material.values())
                                    .map(Material::name)
                                    .collect(Collectors.toList())
                    );
                }
            }
        }

        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        return "<kit> (editor|icon|name|shortname) [<args>]";
    }

    private void saveAndReload(CustomKit ck) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(new File(plugin.getKitsDir(), ck.getId() + ".json")),
                        StandardCharsets.UTF_8
                )
        )) {
            FileUtil.createGson().toJson(ck, CustomKit.class, writer);
            writer.flush();
            plugin.getCustomKitManager().reload();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
