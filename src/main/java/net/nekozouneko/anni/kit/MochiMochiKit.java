package net.nekozouneko.anni.kit;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.item.NexusCompass;
import net.nekozouneko.anni.item.StunGrenade;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import net.nekozouneko.commons.spigot.inventory.special.LeatherArmorBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class MochiMochiKit extends AbstractKit {

    MochiMochiKit() {
        super(
                "mochimochi", "MOC", "kit.mochimochi.name",
                Material.HONEY_BLOCK.name(),
                ANNIPlugin.getInstance().getMessageManager().buildList("kit.mochimochi.about")
        );
    }

    @Override
    public ItemStack[] getKitContents() {
        ItemStack[] inv = new ItemStack[41];

        inv[0] = ItemStackBuilder.of(Material.WOODEN_SWORD).build();
        inv[1] = ItemStackBuilder.of(Material.STONE_PICKAXE).build();
        inv[2] = ItemStackBuilder.of(Material.STONE_AXE).build();
        inv[3] = ItemStackBuilder.of(Material.STONE_SHOVEL).build();
        inv[6] = ItemStackBuilder.of(Material.BREAD).amount(16).build();
        inv[7] = StunGrenade.builder()
                .amount(5)
                .persistentData(new NamespacedKey(ANNIPlugin.getInstance(), "no-remove"), PersistentDataType.INTEGER, 1)
                .build();
        inv[8] = NexusCompass.builder()
                .persistentData(new NamespacedKey(ANNIPlugin.getInstance(), "no-remove"), PersistentDataType.INTEGER, 1)
                .build();

        inv[39] = LeatherArmorBuilder.of(Material.LEATHER_HELMET).build();
        inv[38] = LeatherArmorBuilder.of(Material.LEATHER_CHESTPLATE).build();
        inv[37] = LeatherArmorBuilder.of(Material.LEATHER_LEGGINGS).build();
        inv[36] = LeatherArmorBuilder.of(Material.LEATHER_BOOTS).build();

        return inv;
    }
}
