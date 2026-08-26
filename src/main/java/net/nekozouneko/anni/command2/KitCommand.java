package net.nekozouneko.anni.command2;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.gui.kit.KitSelector;
import net.nekozouneko.anni.listener.PlayerDamageListener;
import org.bukkit.entity.Player;

public class KitCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> get(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder
                .requires(stack -> stack.getSender() instanceof Player && stack.getSender().hasPermission("anni.command.kit"))
                .executes(stack -> {
                    Player player = (Player) stack.getSource().getSender();

                    if (PlayerDamageListener.isFighting(player)) {
                        throw new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
                                ANNIPlugin.getInstance().getTranslationManager()
                                        .component(player, "command.error.now_fighting")
                        )).create();
                    }

                    new KitSelector(ANNIPlugin.getInstance(), player, 1).open();

                    return Command.SINGLE_SUCCESS;
                });
    }
}
