package net.nekozouneko.anni.command2;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.gui.shop.CombatShop;
import org.bukkit.entity.Player;

public class CombatShopCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> get(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder
                .requires(stack -> stack.getSender() instanceof Player && stack.getSender().hasPermission("anni.command.combatshop") && ANNIPlugin.getInstance().getCurrentGame().getState().isInArena())
                .executes(stack -> {
                    Player player = (Player) stack.getSource().getSender();

                    new CombatShop(ANNIPlugin.getInstance(), player).open();

                    return Command.SINGLE_SUCCESS;
                });
    }
}
