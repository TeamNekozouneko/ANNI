package net.nekozouneko.anni.kit;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.item.DefenseArtifact;
import net.nekozouneko.anni.item.NexusCompass;
import net.nekozouneko.anni.util.CmnUtil;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import net.nekozouneko.commons.spigot.inventory.special.LeatherArmorBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Locale;

public class DefenseKit extends InternalKit {

    DefenseKit() {
        super("defense", "kit.defense.name", "kit.defense.description", "DFS", Material.SHIELD);
    }

    @Override
    public ItemStack[] getKitContents(Locale locale) {
        ItemStack[] inv = new ItemStack[41];

        inv[0] = ItemStackBuilder.of(Material.WOODEN_SWORD)
                .enchant(Enchantment.KNOCKBACK, 2, false)
                .build();
        inv[1] = ItemStackBuilder.of(Material.STONE_PICKAXE).build();
        inv[2] = ItemStackBuilder.of(Material.STONE_AXE).build();
        inv[3] = ItemStackBuilder.of(Material.STONE_SHOVEL).build();

        inv[6] = ItemStackBuilder.of(Material.BREAD).amount(16).build();
        inv[7] = DefenseArtifact.get(locale);
        CmnUtil.editPDC(inv[7], c -> c.set(new NamespacedKey(ANNIPlugin.getInstance(), "no-remove"), PersistentDataType.INTEGER, 1));
        inv[8] = NexusCompass.get(locale);
        CmnUtil.editPDC(inv[8], c -> c.set(new NamespacedKey(ANNIPlugin.getInstance(), "no-remove"), PersistentDataType.INTEGER, 1));

        inv[39] = LeatherArmorBuilder.of(Material.CHAINMAIL_HELMET).build();
        inv[38] = LeatherArmorBuilder.of(Material.LEATHER_CHESTPLATE).build();
        inv[37] = LeatherArmorBuilder.of(Material.CHAINMAIL_LEGGINGS).build();
        inv[36] = LeatherArmorBuilder.of(Material.CHAINMAIL_BOOTS).build();

        return inv;
    }
}
