package net.nekozouneko.anniv2.kit;

import net.nekozouneko.anniv2.kit.items.StunGrenade;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import net.nekozouneko.commons.spigot.inventory.special.LeatherArmorBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class MochiMochiKit extends AbsANNIKit{

    MochiMochiKit() {
        super("mochimochi", "MOC", "kit.mochimochi.name");
    }

    @Override
    public ItemStack[] getKitContents() {
        ItemStack[] inv = new ItemStack[41];

        inv[0] = ItemStackBuilder.of(Material.WOODEN_SWORD).build();
        inv[1] = ItemStackBuilder.of(Material.STONE_PICKAXE).build();
        inv[2] = ItemStackBuilder.of(Material.STONE_AXE).build();
        inv[3] = ItemStackBuilder.of(Material.STONE_SHOVEL).build();
        inv[8] = StunGrenade.get(10);

        inv[39] = LeatherArmorBuilder.of(Material.LEATHER_HELMET).build();
        inv[38] = LeatherArmorBuilder.of(Material.LEATHER_CHESTPLATE).build();
        inv[37] = LeatherArmorBuilder.of(Material.LEATHER_LEGGINGS).build();
        inv[36] = LeatherArmorBuilder.of(Material.LEATHER_BOOTS).build();

        return inv;
    }

    @Override
    public List<String> getBlackList() {
        return Collections.emptyList();
    }
}
