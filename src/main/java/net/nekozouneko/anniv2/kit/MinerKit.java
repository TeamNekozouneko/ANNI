package net.nekozouneko.anniv2.kit;

import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import net.nekozouneko.commons.spigot.inventory.special.LeatherArmorBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

public class MinerKit extends AbsANNIKit {

    public MinerKit() {
        super(
                "miner", "MNR", "kit.miner.name",
                Material.IRON_PICKAXE.name(), Collections.emptyList()
        );
    }

    @Override
    public ItemStack[] getKitContents() {
        ItemStack[] inv = new ItemStack[41];

        inv[0] = ItemStackBuilder.of(Material.WOODEN_SWORD).build();
        inv[1] = ItemStackBuilder.of(Material.IRON_PICKAXE)
                .enchant(Enchantment.DURABILITY, 1, false)
                .enchant(Enchantment.LOOT_BONUS_BLOCKS, 1, false)
                .build();
        inv[2] = ItemStackBuilder.of(Material.STONE_AXE).build();
        inv[3] = ItemStackBuilder.of(Material.STONE_SHOVEL).build();

        inv[39] = LeatherArmorBuilder.of(Material.LEATHER_HELMET).build();
        inv[38] = LeatherArmorBuilder.of(Material.LEATHER_CHESTPLATE).build();
        inv[37] = LeatherArmorBuilder.of(Material.LEATHER_LEGGINGS).build();
        inv[36] = LeatherArmorBuilder.of(Material.LEATHER_BOOTS).build();

        return inv;
    }
}
