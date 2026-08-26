package net.nekozouneko.anni.command2;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.spectator.SpectatorManager;
import net.nekozouneko.anni.gui.shop.PointCharger;
import org.bukkit.entity.Player;

public final class ChargeCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> get(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder
                .requires(stack -> stack.getSender() instanceof Player && stack.getSender().hasPermission("anni.command.charge"))
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();

                    if (!(ANNIPlugin.getInstance().getCurrentGame().getState().getId() > 0) || SpectatorManager.isSpectating(player)) {
                        return Command.SINGLE_SUCCESS;
                    }

                    new PointCharger(ANNIPlugin.getInstance(), player).open();

                    return Command.SINGLE_SUCCESS;
                });
    }

}
