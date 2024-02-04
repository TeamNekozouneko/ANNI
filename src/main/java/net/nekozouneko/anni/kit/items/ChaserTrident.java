package net.nekozouneko.anni.kit.items;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.kit.ANNIKit;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class ChaserTrident implements Listener {

    public static ItemStackBuilder builder() {
        return ItemStackBuilder.of(Material.TRIDENT)
                .attribute(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(), "damagerm", 4D, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND))
                .enchant(Enchantment.LOYALTY, 1, false)
                .itemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS)
                .persistentData(new NamespacedKey(ANNIPlugin.getInstance(), "special-item"), PersistentDataType.STRING, "chaser-trident");
    }

    @EventHandler
    public void onHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Trident)) return;
        if (e.getEntity().getShooter() == null || !(e.getEntity().getShooter() instanceof Player)) return;

        Player shooter = (Player) e.getEntity().getShooter();

        ANNIKit kit = ANNIKit.get(ANNIPlugin.getInstance().getCurrentGame().getKit(shooter));

        if (kit != ANNIKit.CHASER) return;

        e.getEntity().setInvulnerable(true);
        e.getEntity().getWorld().createExplosion(e.getEntity().getLocation(), 2, false, false, shooter);
    }

}
