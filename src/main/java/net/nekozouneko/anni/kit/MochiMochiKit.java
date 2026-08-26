package net.nekozouneko.anni.kit;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.item.NexusCompass;
import net.nekozouneko.anni.item.StunGrenade;
import net.nekozouneko.anni.util.CmnUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Locale;

public class MochiMochiKit extends InternalKit {

    MochiMochiKit() {
        super("mochimochi", "kit.mochimochi.name", "kit.mochimochi.description", "MOC", Material.HONEY_BLOCK);
    }

    @Override
    public ItemStack[] getKitContents(Locale locale) {
        ItemStack[] inv = new ItemStack[41];

        inv[0] = ItemStack.of(Material.WOODEN_SWORD);
        inv[1] = ItemStack.of(Material.STONE_PICKAXE);
        inv[2] = ItemStack.of(Material.STONE_AXE);
        inv[3] = ItemStack.of(Material.STONE_SHOVEL);
        inv[6] = ItemStack.of(Material.BREAD).add(15);
        inv[7] = StunGrenade.get(locale).add(5);
        CmnUtil.editPDC(inv[7], c -> c.set(new NamespacedKey(ANNIPlugin.getInstance(), "no-remove"), PersistentDataType.INTEGER, 1));
        inv[8] = NexusCompass.get(locale);
        CmnUtil.editPDC(inv[8], c -> c.set(new NamespacedKey(ANNIPlugin.getInstance(), "no-remove"), PersistentDataType.INTEGER, 1));

        inv[39] = ItemStack.of(Material.LEATHER_HELMET);
        inv[38] = ItemStack.of(Material.LEATHER_CHESTPLATE);
        inv[37] = ItemStack.of(Material.LEATHER_LEGGINGS);
        inv[36] = ItemStack.of(Material.LEATHER_BOOTS);

        return inv;
    }
}
