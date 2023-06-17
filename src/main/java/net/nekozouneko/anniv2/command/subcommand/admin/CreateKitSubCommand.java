package net.nekozouneko.anniv2.command.subcommand.admin;

import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.command.ASubCommand;
import net.nekozouneko.anniv2.kit.custom.CustomKit;
import net.nekozouneko.anniv2.message.MessageManager;
import net.nekozouneko.anniv2.util.FileUtil;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class CreateKitSubCommand extends ASubCommand {

    private final MessageManager mm = ANNIPlugin.getInstance().getMessageManager();

    @Override
    public boolean execute(CommandSender sender, List<String> args) {
        if (args.size() < 2) return false;

        String id = args.get(0);
        String shortName = args.get(1);
        String name = args.size() >= 3 ? String.join(" ", args.subList(2, args.size())) : id;

        if (ANNIPlugin.getInstance().getCustomKitManager().getKit(id) != null) {
            sender.sendMessage(mm.build("command.err.kit_exists"));
            return true;
        }

        CustomKit ck = new CustomKit(id, shortName, Material.CHEST, name, Collections.emptyList(), new ItemStack[0], Collections.emptyList());

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(
                        new File(ANNIPlugin.getInstance().getKitsDir(), id + ".json")
                ), StandardCharsets.UTF_8)
        )) {
            FileUtil.createGson().toJson(ck, CustomKit.class, writer);
            writer.flush();
            ANNIPlugin.getInstance().getCustomKitManager().reload();
            if (ANNIPlugin.getInstance().getCustomKitManager().getKit(id) != null) {
                sender.sendMessage(mm.build("command.createkit.success", name, shortName));
            }
            else sender.sendMessage(mm.build("command.err.unknown"));
        }
        catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage(mm.build("command.err.ioe"));
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        return "<id> <shortName> [name]";
    }
}
