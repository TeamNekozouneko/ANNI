package net.nekozouneko.anniv2.listener;

import com.google.common.base.Strings;
import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.arena.team.ANNITeam;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scoreboard.Team;

public class AsyncPlayerChatListener implements Listener {

    private final ANNIPlugin plugin = ANNIPlugin.getInstance();

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (BlockBreakListener.getQueuedOnDamageMap().containsKey(e.getPlayer().getUniqueId())) {
            if (e.getMessage().equalsIgnoreCase("cancel")) {
                BlockBreakListener.getQueuedOnDamageMap().remove(e.getPlayer().getUniqueId());
                e.setCancelled(true);
                return;
            }
        }

        ANNITeam at = plugin.getCurrentGame().getTeamByPlayer(e.getPlayer());
        if (at != null && !e.getMessage().startsWith("!")) {
            Team t = plugin.getCurrentGame().getTeam(at);

            try {
                e.getRecipients().clear();
                e.getRecipients().addAll(plugin.getCurrentGame().getTeamPlayers(at));
                String form = plugin.getMessageManager().build("chat.team.format",
                        t != null ? Strings.nullToEmpty(t.getPrefix()) + t.getColor() : ""
                );
                e.setFormat(form);
                plugin.getLogger().info(String.format(form, e.getPlayer().getName(), e.getMessage()));
            }
            catch (Exception e1) {
                e.setCancelled(true);
                String mes = plugin.getMessageManager().build("chat.team",
                        t != null ?
                                Strings.nullToEmpty(t.getPrefix()) + t.getColor() + e.getPlayer().getName()
                                : e.getPlayer().getName(),
                        e.getMessage()
                );
                plugin.getCurrentGame().broadcast(mes, at);
                plugin.getLogger().info(mes);
            }
        }
        else {
            String prefix;
            String username;
            if (at != null) {
                e.setMessage(e.getMessage().substring(1));
                Team t = plugin.getCurrentGame().getTeam(at);

                prefix = Strings.nullToEmpty(t.getPrefix()) + t.getColor();
                username = prefix + e.getPlayer().getName();
            }
            else {
                prefix = "";
                username = e.getPlayer().getName();
            }


            try {
                e.getRecipients().clear();
                e.getRecipients().addAll(plugin.getCurrentGame().getTeamPlayers(at));
                String form = plugin.getMessageManager().build("chat.global.format", prefix);
                e.setFormat(form);
                plugin.getLogger().info(String.format(form, e.getPlayer().getName(), e.getMessage()));
            }
            catch (Exception e1) {
                e.setCancelled(true);
                String mes = plugin.getMessageManager().build("chat.global",
                        username,
                        e.getMessage()
                );
                plugin.getCurrentGame().broadcast(mes);
                plugin.getLogger().info(mes);
            }
        }
    }

}
