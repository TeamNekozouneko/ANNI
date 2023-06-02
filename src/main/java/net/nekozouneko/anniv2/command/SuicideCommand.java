package net.nekozouneko.anniv2.command;

import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.listener.PlayerDamageListener;
import net.nekozouneko.anniv2.message.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class SuicideCommand implements CommandExecutor, TabCompleter {

    private final Map<UUID, Long> cooldown = new HashMap<>();
    private final MessageManager mm = ANNIPlugin.getInstance().getMessageManager();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(mm.build("command.err.player_only"));
            return true;
        }

        Player p = (Player) sender;

        if (cooldown.get(p.getUniqueId()) == null || System.currentTimeMillis() >= cooldown.get(p.getUniqueId())) {
            if (PlayerDamageListener.isFighting(p)) {
                sender.sendMessage(mm.build("command.err.now_fighting"));
                return true;
            }

            p.damage(p.getHealth() * 2);
            cooldown.put(p.getUniqueId(), System.currentTimeMillis() + 10000);
        }
        else sender.sendMessage(mm.build(
                "command.err.cooldown",
                Long.toString((cooldown.get(p.getUniqueId()) - System.currentTimeMillis()) / 1000)
        ));

        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return Collections.emptyList();
    }
}
