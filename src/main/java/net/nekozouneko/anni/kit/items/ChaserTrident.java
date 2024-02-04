package net.nekozouneko.anni.kit.items;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class ChaserTrident {

    public static ItemStackBuilder builder() {
        return ItemStackBuilder.of(Material.TRIDENT)
                .attribute(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(), "damagerm", 3D, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND))
                .attribute(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(), "addcool", 1D, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND))
                .enchant(Enchantment.LOYALTY, 1, false)
                .itemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS)
                .persistentData(new NamespacedKey(ANNIPlugin.getInstance(), "special-item"), PersistentDataType.STRING, "chaser-trident");
    }


}
