package net.nekozouneko.anni.item;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.persistence.PersistentDataType;

public class ChaserTrident {

    public static ItemStackBuilder builder() {
        return ItemStackBuilder.of(Material.TRIDENT)
                .attribute(Attribute.ATTACK_DAMAGE, new AttributeModifier(new NamespacedKey(ANNIPlugin.getInstance(), "damagerm"), 3D, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND))
                .attribute(Attribute.ATTACK_SPEED, new AttributeModifier(new NamespacedKey(ANNIPlugin.getInstance(), "addcool"), 1D, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND))
                .enchant(Enchantment.LOYALTY, 1, false)
                .itemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS)
                .persistentData(new NamespacedKey(ANNIPlugin.getInstance(), "special-item"), PersistentDataType.STRING, "chaser-trident");
    }


}
