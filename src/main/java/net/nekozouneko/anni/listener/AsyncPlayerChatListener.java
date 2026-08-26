package net.nekozouneko.anni.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.ANNIArena;
import net.nekozouneko.anni.arena.spectator.SpectatorManager;
import net.nekozouneko.anni.arena.team.ANNITeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AsyncPlayerChatListener implements Listener {

    private final ANNIPlugin plugin = ANNIPlugin.getInstance();

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        String plain = PlainTextComponentSerializer.plainText().serialize(e.message());

        if (BlockBreakListener.getQueuedOnDamageMap().containsKey(e.getPlayer().getUniqueId())) {
            if (plain.equalsIgnoreCase("cancel")) {
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

        ANNITeam at = plugin.getCurrentGame().getTeamManager().getTeamColorByPlayer(e.getPlayer().getUniqueId());
        var translationManager = ANNIPlugin.getInstance().getTranslationManager();

        if (SpectatorManager.isSpectating(e.getPlayer())) {
            if (plain.startsWith("!") && e.getPlayer().hasPermission("anni.mod.global_chat_on_spectator")) {
                e.message(Component.text(plain.substring(1)));
                globalChat(e);
                return;
            }

            spectatorChat(e);
            return;
        }

        if (at != null) {
            if (plain.startsWith("!")) {
                e.message(Component.text(plain.substring(1)));
                globalChat(e);
                return;
            }

            if (plain.startsWith("@")) {
                Matcher matcher = Pattern.compile("^@([^ ]{2,20}) (.+)$").matcher(plain);
                if (matcher.find()) {
                    Player receiver = Bukkit.getPlayer(matcher.group(1));

                    e.setCancelled(true);
                    if (receiver != null) {
                        if (receiver.equals(e.getPlayer())) {
                            e.getPlayer().sendMessage(translationManager.component(e.getPlayer(), "command.error.self_message"));
                            return;
                        }

                        if (plugin.getCurrentGame().getTeamPlayers(
                                plugin.getCurrentGame().getTeamManager().getTeamColorByPlayer(e.getPlayer().getUniqueId())
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
                                translationManager.component(e.getPlayer(),"command.error.non_equal_team")
                        );
                    }
                    else e.getPlayer().sendMessage(
                            translationManager.component(e.getPlayer(), "command.error.player_not_found", matcher.group(1))
                    );

                    return;
                }
            }

            teamChat(at, e);
            return;
        }

        globalChat(e);
    }

    private void globalChat(AsyncChatEvent e) {
        ANNITeam at = ANNIPlugin.getInstance().getCurrentGame().getTeamManager().getTeamColorByPlayer(e.getPlayer().getUniqueId());
        var tm = ANNIPlugin.getInstance().getTranslationManager();

        var name = Component.text();

        if (at != null) {
            name.append(tm.component(at.getPrefix()).color(at.getColor()));
        }

        name.append(e.getPlayer().displayName()).color(at != null ? at.getColor() : NamedTextColor.WHITE);

        try {
            e.renderer((sender, senderName, message, audience) ->
                    tm.component(audience instanceof Player player ? player.locale() : null, "chat.global", name.asComponent(), message)
            );
        }
        catch (UnsupportedOperationException e1) {
            e.setCancelled(true);
            Bukkit.getOnlinePlayers().forEach(player ->
                    player.sendMessage(tm.component(player, "chat.global", name.asComponent(), e.message()))
            );
        }
    }

    private void teamChat(ANNITeam team, AsyncChatEvent e) {
        var tm = ANNIPlugin.getInstance().getTranslationManager();

        try {
            e.viewers().clear();
            e.viewers().addAll(plugin.getCurrentGame().getTeamPlayers(team));
            e.viewers().add(Bukkit.getConsoleSender());

            e.renderer((sender, senderName, message, audience) -> {
                Locale locale = audience instanceof Player player ? player.locale() : null;

                return tm.component(locale, "chat.team", tm.component(locale, team.getNameKey()), senderName, message);
            });
        }
        catch (UnsupportedOperationException e1) {
            e.setCancelled(true);

            plugin.getCurrentGame().getTeamPlayers(team).forEach(player ->
                    player.sendMessage(tm.component(player, "chat.team", tm.component(player, team.getNameKey()), e.getPlayer().displayName(), e.message())));
            Bukkit.getConsoleSender().sendMessage(tm.component("chat.team", tm.component(team.getNameKey()), e.getPlayer().displayName(), e.message()));
        }
    }

    private void spectatorChat(AsyncChatEvent e) {
        var translationManager = ANNIPlugin.getInstance().getTranslationManager();

        try {
            e.viewers().clear();
            e.viewers().addAll(
                    SpectatorManager.getPlayers().stream()
                            .map(Bukkit::getPlayer)
                            .filter(Objects::nonNull)
                            .toList()
            );
            e.viewers().addAll(
                    SpectatorManager.getWatchablePlayers().stream()
                            .map(Bukkit::getPlayer)
                            .filter(Objects::nonNull)
                            .toList()
            );
            e.viewers().add(Bukkit.getConsoleSender());

            e.renderer(((sender, senderName, message, audience) ->
                    translationManager.component(audience instanceof Player player ? player.locale() : null,
                            "chat.spectator", senderName, message
                    )
            ));
        }
        catch (UnsupportedOperationException e1) {
            e.setCancelled(true);

            SpectatorManager.getPlayers().stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .forEach(p -> p.sendMessage(translationManager.component(
                            p,
                            "chat.spectator",
                            e.getPlayer().displayName(),
                            e.message()
                    )));

            SpectatorManager.getWatchablePlayers().stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .forEach(p -> p.sendMessage(translationManager.component(
                            p,
                            "chat.spectator",
                            e.getPlayer().displayName(),
                            e.message()
                    )));
            Bukkit.getConsoleSender().sendMessage(translationManager.component("chat.spectator",
                    e.getPlayer().displayName(),
                    e.message()
            ));
        }
    }

}
