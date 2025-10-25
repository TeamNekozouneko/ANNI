package net.nekozouneko.anni.command2;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import net.kyori.adventure.text.Component;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.point.LevelManager;
import net.nekozouneko.anni.util.CmnUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PlayerCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> get(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder
                .requires(stack -> stack.getSender().hasPermission("anni.command.player"))
                .executes(PlayerCommand::sendPlayerInformation)
                .then(Commands.argument("player", ArgumentTypes.playerProfiles())
                        .requires(stack -> stack.getSender().hasPermission("anni.command.player.others"))
                        .executes(PlayerCommand::sendPlayerInformation)
                );
    }

    private static int sendPlayerInformation(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        PlayerProfileListResolver resolver;
        try {
            resolver = ctx.getArgument("player", PlayerProfileListResolver.class);
        }
        catch (IllegalArgumentException e) {
            resolver = null;
        }

        Locale locale = ctx.getSource().getSender() instanceof Player player ? player.locale() : null;

        List<PlayerProfile> profiles = new ArrayList<>();

        if (resolver != null) profiles.addAll(resolver.resolve(ctx.getSource()));
        else {
            try {
                profiles.add(((Player) ctx.getSource()).getPlayerProfile());
            }
            catch (ClassCastException cast) {
                throw new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
                        ANNIPlugin.getInstance().getTranslationManager().component("command.error.player_only")
                )).createWithContext(new StringReader(ctx.getInput()));
            }
        }

        profiles.forEach(profile -> {
            ctx.getSource().getSender().sendMessage(getProfile(locale, profile));
        });

        return Command.SINGLE_SUCCESS;
    }

    private static Component getProfile(Locale locale, PlayerProfile profile) {
        ANNIPlugin plugin = ANNIPlugin.getInstance();
        var translation = plugin.getTranslationManager();

        int lvl = plugin.getLevelManager().getLevel(profile.getId());
        long exp = plugin.getLevelManager().getExp(profile.getId());

        double progress = (double) exp / LevelManager.calculateExpForNextLevel(lvl);

        return translation.componentLines(locale, "command.player.result",
                profile.getName(), lvl, exp, LevelManager.calculateExpForNextLevel(lvl),
                CmnUtil.progressBar(progress, 20),
                String.format("%,d", plugin.getPointManager().getPoint(Bukkit.getOfflinePlayer(profile.getId())))
        );
    }

}
