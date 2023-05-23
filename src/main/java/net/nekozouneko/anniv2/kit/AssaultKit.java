package net.nekozouneko.anniv2.kit;

import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import net.nekozouneko.commons.spigot.inventory.special.LeatherArmorBuilder;
import net.nekozouneko.commons.spigot.inventory.special.PotionBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.Collections;
import java.util.List;

public final class AssaultKit extends AbsANNIKit {

    AssaultKit() {
        super("assault", "AST", "kit.assault.name");
    }

    @Override
    public ItemStack[] getKitContents() {
        ItemStack[] inv = new ItemStack[41];

        inv[0] = ItemStackBuilder.of(Material.STONE_SWORD).build();
        inv[1] = ItemStackBuilder.of(Material.WOODEN_PICKAXE).build();
        inv[2] = ItemStackBuilder.of(Material.WOODEN_AXE).build();
        inv[3] = ItemStackBuilder.of(Material.WOODEN_SHOVEL).build();
        inv[6] = PotionBuilder.of(Material.POTION)
                .basePotionData(new PotionData(PotionType.STRENGTH))
                .build();
        inv[7] = PotionBuilder.of(Material.POTION)
                .basePotionData(new PotionData(PotionType.REGEN, false, true))
                .build();
        inv[8] = PotionBuilder.of(Material.POTION)
                .basePotionData(new PotionData(PotionType.REGEN, false, true))
                .build();

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
