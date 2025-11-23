package net.nekozouneko.anni.listener;

import net.nekozouneko.anni.ANNIPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataType;

public class ProjectileHitListener implements Listener {

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball)) return;

        if (event.getHitEntity() == null || !(event.getHitEntity() instanceof Damageable dmg)) return;

        var container = snowball.getPersistentDataContainer();
        String id = container.get(new NamespacedKey(ANNIPlugin.getInstance(), "special-item"), PersistentDataType.STRING);

        if (!"axes".equals(id)) return;

        Material type = snowball.getItem().getType();

        double damage;
        switch (type) {
            case WOODEN_AXE, GOLDEN_AXE-> damage = 3;
            case STONE_AXE -> damage = 4;
            case IRON_AXE, DIAMOND_AXE, NETHERITE_AXE -> damage = 5;
            default -> {
                return;
            }
        }

        dmg.damage(damage, snowball);
    }
}
