package net.nekozouneko.anni.listener;

import com.google.common.base.Strings;
import net.md_5.bungee.api.ChatColor;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.ANNIArena;
import net.nekozouneko.anni.arena.spectator.SpectatorManager;
import net.nekozouneko.anni.arena.team.ANNITeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scoreboard.Team;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

        ANNIArena arena = plugin.getCurrentGame();

        if (!arena.getState().isInArena()) {
            globalChat(e);
            return;
        }

        // これからゲーム内のみ

        ANNITeam at = plugin.getCurrentGame().getTeamByPlayer(e.getPlayer());

        if (SpectatorManager.isSpectating(e.getPlayer())) {
            if (e.getMessage().startsWith("!") && e.getPlayer().hasPermission("anni.mod.global_chat_on_spectator"))
                globalChat(e);

            spectatorChat(e);
            return;
        }

        if (at != null) {
            if (e.getMessage().startsWith("!")) {
                globalChat(e);
                return;
            }

            if (e.getMessage().startsWith("@")) {
                Matcher matcher = Pattern.compile("^@([^ ]{2,20}) (.+)$").matcher(e.getMessage());
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

            teamChat(at, e);
            return;
        }

        globalChat(e);
    }

    private boolean canSendPrivateMessage(Player from, Player to) {
        ANNIArena arena = ANNIPlugin.getInstance().getCurrentGame();

        boolean equalsTeam = arena.getTeamByPlayer(from) == arena.getTeamByPlayer(to);
        boolean isSpectator = SpectatorManager.isSpectating(from) && SpectatorManager.isSpectating(to);
        boolean isNotSpectator = !SpectatorManager.isSpectating(from) && !SpectatorManager.isSpectating(to);

        return (equalsTeam && isNotSpectator) || isSpectator;
    }

    private void globalChat(AsyncPlayerChatEvent e) {
        ANNITeam at = ANNIPlugin.getInstance().getCurrentGame().getTeamByPlayer(e.getPlayer());

        String prefix;
        String username;
        if (at != null) {
            e.setMessage(e.getMessage().substring(1));
            Team t = plugin.getCurrentGame().getTeam(at);

            prefix = t.getColor() + Strings.nullToEmpty(t.getPrefix());
            username = prefix + ChatColor.RESET
                    + (Strings.nullToEmpty(t.getPrefix()).isEmpty() ? "" : " ")
                    + t.getColor() + e.getPlayer().getName();
        }
        else {
            prefix = "";
            username = e.getPlayer().getName();
        }


        try {
            String form = plugin.getMessageManager().build("chat.global.format", prefix.isEmpty() ? "" : prefix + "§r ");
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

    private void teamChat(ANNITeam team, AsyncPlayerChatEvent e) {
        Team t = ANNIPlugin.getInstance().getCurrentGame().getTeam(team);

        try {
            e.getRecipients().clear();
            e.getRecipients().addAll(plugin.getCurrentGame().getTeamPlayers(team));
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
            plugin.getCurrentGame().broadcast(mes, team);
        }
    }

    private void spectatorChat(AsyncPlayerChatEvent e) {
        try {
            e.getRecipients().clear();
            e.getRecipients().addAll(
                    SpectatorManager.getPlayers().stream()
                            .map(Bukkit::getPlayer)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList())
            );
            e.getRecipients().addAll(
                    SpectatorManager.getWatchablePlayers().stream()
                            .map(Bukkit::getPlayer)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList())
            );
            String form = plugin.getMessageManager().build("chat.spectator.format");
            e.setFormat(form);
        }
        catch (UnsupportedOperationException e1) {
            e.setCancelled(true);
            String mes = plugin.getMessageManager().build("chat.spectator",
                    e.getPlayer().getName(),
                    e.getMessage()
            );

            SpectatorManager.getPlayers().stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .forEach(p -> p.sendMessage(mes));

            SpectatorManager.getWatchablePlayers().stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .forEach(p -> p.sendMessage(mes));
            Bukkit.getConsoleSender().sendMessage(mes);
        }
    }

}
