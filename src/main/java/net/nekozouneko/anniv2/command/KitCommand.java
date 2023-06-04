package net.nekozouneko.anniv2.command;

import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.gui.KitSelector;
import net.nekozouneko.anniv2.listener.PlayerDamageListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class KitCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            if (PlayerDamageListener.isFighting((Player) sender)) {
                sender.sendMessage(ANNIPlugin.getInstance().getMessageManager()
                        .build("command.err.now_fighting")
                );
            }
            else new KitSelector(ANNIPlugin.getInstance(), (Player) sender).open();
        }
        else sender.sendMessage(ANNIPlugin.getInstance().getMessageManager().build("command.err.player_only"));

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return Collections.emptyList();
    }
}
