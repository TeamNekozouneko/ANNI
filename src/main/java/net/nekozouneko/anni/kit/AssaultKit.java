package net.nekozouneko.anni.kit;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.item.NexusCompass;
import net.nekozouneko.anni.util.CmnUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Locale;

public final class AssaultKit extends InternalKit {

    AssaultKit() {
        super("assault", "kit.assault.name", "kit.assault.description", "AST", Material.IRON_SWORD);
    }

    @Override
    public ItemStack[] getKitContents(Locale locale) {
        ItemStack[] inv = new ItemStack[41];

        inv[0] = ItemStack.of(Material.STONE_SWORD);
        inv[1] = ItemStack.of(Material.STONE_PICKAXE);
        inv[2] = ItemStack.of(Material.STONE_AXE);
        inv[3] = ItemStack.of(Material.STONE_SHOVEL);
        inv[7] = ItemStack.of(Material.BREAD).add(15);
        inv[8] = NexusCompass.get(locale);
        CmnUtil.editPDC(inv[8], c -> c.set(new NamespacedKey(ANNIPlugin.getInstance(), "no-remove"), PersistentDataType.INTEGER, 1));

        inv[39] = ItemStack.of(Material.LEATHER_HELMET);
        inv[38] = ItemStack.of(Material.LEATHER_CHESTPLATE);
        inv[37] = ItemStack.of(Material.LEATHER_LEGGINGS);
        inv[36] = ItemStack.of(Material.LEATHER_BOOTS);

        return inv;
    }
}
