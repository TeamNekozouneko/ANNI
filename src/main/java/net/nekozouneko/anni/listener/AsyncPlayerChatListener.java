package net.nekozouneko.anni.listener;

import com.google.common.base.Strings;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.team.ANNITeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scoreboard.Team;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

            if (e.getMessage().startsWith("@")) {
                Matcher matcher = Pattern.compile("^@(.{2,20}) (.+)$").matcher(e.getMessage());
                if (matcher.find()) {
                    Player receiver = Bukkit.getPlayer(matcher.group(1));

                    e.setCancelled(true);
                    if (receiver != null) {
                        if (receiver.equals(e.getPlayer())) {
                            e.getPlayer().sendMessage(plugin.getMessageManager().build(
                                    "command.err.self_message"
                            ));
                            return;
                        }

                        if (plugin.getCurrentGame().getTeamPlayers(
                                plugin.getCurrentGame().getTeamByPlayer(e.getPlayer())
                        ).contains(receiver)) {
                            String senderMessage = plugin.getMessageManager().build("chat.tell.send",
                                    receiver.getName(),
                                    matcher.group(2)
                            );
                            e.getPlayer().sendMessage(senderMessage);
                            plugin.getLogger().info(senderMessage);
                            receiver.sendMessage(
                                    plugin.getMessageManager().build("chat.tell.receive",
                                            e.getPlayer().getName(),
                                            matcher.group(2)
                                    )
                            );

                            return;
                        }
                        else e.getPlayer().sendMessage(
                                plugin.getMessageManager().build("command.err.non_equal_team")
                        );
                    }
                    else e.getPlayer().sendMessage(
                            plugin.getMessageManager().build("command.err.player_not_found", matcher.group(1))
                    );

                    return;
                }
            }

            try {
                e.getRecipients().clear();
                e.getRecipients().addAll(plugin.getCurrentGame().getTeamPlayers(at));
                String form = plugin.getMessageManager().build("chat.team.format",
                        t.getColor() + t.getDisplayName()
                );
                e.setFormat(form);
            }
            catch (UnsupportedOperationException e1) {
                e.setCancelled(true);
                String mes = plugin.getMessageManager().build("chat.team",
                        t.getColor() + t.getDisplayName(),
                        e.getPlayer().getName(),
                        e.getMessage()
                );
                plugin.getCurrentGame().broadcast(mes, at);
            }
        }
        else {
            String prefix;
            String username;
            if (at != null) {
                e.setMessage(e.getMessage().substring(1));
                Team t = plugin.getCurrentGame().getTeam(at);

                prefix = Strings.nullToEmpty(t.getPrefix()) + t.getColor();
                username = prefix + "§r " + e.getPlayer().getName();
            }
            else {
                prefix = "";
                username = e.getPlayer().getName();
            }


            try {
                String form = plugin.getMessageManager().build("chat.global.format", prefix + "§r ");
                e.setFormat(form);
            }
            catch (UnsupportedOperationException e1) {
                e.setCancelled(true);
                String mes = plugin.getMessageManager().build("chat.global",
                        username,
                        e.getMessage()
                );
                plugin.getCurrentGame().broadcast(mes);
            }
        }
    }

}
