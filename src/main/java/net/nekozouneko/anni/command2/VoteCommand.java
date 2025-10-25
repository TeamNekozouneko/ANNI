package net.nekozouneko.anni.command2;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.nekozouneko.anni.ANNIPlugin;
import org.bukkit.entity.Player;

public final class VoteCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> get(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder
                .requires(stack -> stack.getSender() instanceof Player && stack.getSender().hasPermission("anni.command.vote"))
                .then(Commands.argument("choice", StringArgumentType.word())
                        .executes(VoteCommand::vote)
                        .suggests((stack, suggestBuilder) -> {
                            var voteManager = ANNIPlugin.getInstance().getCurrentGame().getVoteManager();
                            voteManager.getChoices().forEach(suggestBuilder::suggest);

                            return suggestBuilder.buildFuture();
                        })
                );
    }

    private static int vote(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var voteManager = ANNIPlugin.getInstance().getCurrentGame().getVoteManager();
        var translation = ANNIPlugin.getInstance().getTranslationManager();
        Player player = (Player) ctx.getSource().getSender();
        String choice = ctx.getArgument("choice", String.class);

        if (voteManager == null) {
            ctx.getSource().getSender().sendMessage(translation.component(
                    player.locale(), "command.error.cant_vote_now"
            ));

            return Command.SINGLE_SUCCESS;
        }

        if (!voteManager.getChoices().contains(choice)) {
            throw new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
                    translation.component(player.locale(), "command.error.invalid_vote", choice)
            )).create();
        }

        voteManager.vote(player, choice);
        ctx.getSource().getSender().sendMessage(translation.component(
                player.locale(), "command.vote.voted", choice
        ));

        return Command.SINGLE_SUCCESS;
    }

}
