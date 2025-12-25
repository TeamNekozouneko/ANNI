package net.nekozouneko.anni.command2;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Damageable;

public class SuicideCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> get(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder
                .requires(stack -> stack.getSender() instanceof Damageable && stack.getSender().hasPermission("anni.command.suicide"))
                .executes(stack -> {
                    Damageable entity = (Damageable) stack.getSource().getSender();

                    entity.setHealth(0);

                    return Command.SINGLE_SUCCESS;
                });
    }
}
