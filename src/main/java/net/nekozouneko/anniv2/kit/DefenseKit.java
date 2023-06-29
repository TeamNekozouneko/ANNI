package net.nekozouneko.anniv2.kit;

import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import net.nekozouneko.commons.spigot.inventory.special.LeatherArmorBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

public class DefenseKit extends AbsANNIKit{

    DefenseKit() {
        super(
                "defense", "DEF", "kit.defense.name",
                Material.SHIELD.name(), Collections.emptyList()
        );
    }

    @Override
    public ItemStack[] getKitContents() {
        ItemStack[] inv = new ItemStack[41];

        inv[0] = ItemStackBuilder.of(Material.WOODEN_SWORD).build();
        inv[1] = ItemStackBuilder.of(Material.STONE_PICKAXE).build();
        inv[2] = ItemStackBuilder.of(Material.STONE_AXE).build();
        inv[3] = ItemStackBuilder.of(Material.STONE_SHOVEL).build();

        inv[8] = ItemStackBuilder.of(Material.BREAD).amount(16).build();

        inv[39] = LeatherArmorBuilder.of(Material.CHAINMAIL_HELMET).build();
        inv[38] = LeatherArmorBuilder.of(Material.LEATHER_CHESTPLATE).build();
        inv[37] = LeatherArmorBuilder.of(Material.CHAINMAIL_LEGGINGS).build();
        inv[36] = LeatherArmorBuilder.of(Material.CHAINMAIL_BOOTS).build();

        inv[40] = ItemStackBuilder.of(Material.SHIELD).build();

        return inv;
    }
}
