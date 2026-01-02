package net.nekozouneko.anni.item;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.task.CooldownManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import java.util.Locale;

public class Swapper implements Listener {

    public static ItemStack get(Locale locale) {
        var tm = ANNIPlugin.getInstance().getTranslationManager();

        ItemStack item = ItemStack.of(Material.MUSIC_DISC_CAT);
        item.editMeta(meta -> {
            meta.displayName(tm.component(locale, "item.swapper.name"));
            meta.lore(tm.componentList(locale, "item.swapper.lore"));
            meta.getPersistentDataContainer().set(new NamespacedKey(ANNIPlugin.getInstance(), "special-item"), PersistentDataType.STRING, "swapper");
            meta.setEnchantmentGlintOverride(true);
        });

        return item;
    }

    public static boolean isSwapper(PersistentDataHolder holder) {
        if (holder == null) return false;

        NamespacedKey key = new NamespacedKey(ANNIPlugin.getInstance(), "special-item");

        return holder.getPersistentDataContainer()
                .getOrDefault(key, PersistentDataType.STRING, "").equals("swapper");
    }


    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getItem() == null || e.getItem().getType().isAir() || !isSwapper(e.getItem().getItemMeta())) return;

        e.setCancelled(true);

        CooldownManager cm = ANNIPlugin.getInstance().getCooldownManager();

        if (!cm.isCooldownEnd(e.getPlayer().getUniqueId(), CooldownManager.Type.SWAPPER)) {
            e.getPlayer().sendActionBar(
                    ANNIPlugin.getInstance().getTranslationManager().component("actionbar.cooldown.time",
                            cm.getTimeLeftFormatted(e.getPlayer().getUniqueId(), CooldownManager.Type.SWAPPER)
                    )
            );

            return;
        }

        cm.set(e.getPlayer().getUniqueId(), CooldownManager.Type.SWAPPER, 30*1000);

        Snowball snowball = e.getPlayer().launchProjectile(Snowball.class);
        snowball.setItem(ItemStack.of(Material.MUSIC_DISC_CAT));
        snowball.setGravity(false);

        snowball.getPersistentDataContainer().set(new NamespacedKey(ANNIPlugin.getInstance(), "special-item"), PersistentDataType.STRING, "swapper");
        Bukkit.getScheduler().runTaskLater(ANNIPlugin.getInstance(), snowball::remove, 20 * 10);
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (!isSwapper(event.getEntity())) return;

        if (!(event.getHitEntity() instanceof Player hit)) return;

        if (hit.isBlocking()) return;

        Player shooter = (Player) event.getEntity().getShooter();

        if (shooter.isDead() || !shooter.getWorld().equals(hit.getWorld())) return;

        Location shooterLocation = shooter.getLocation().clone();
        Location hitLocation = hit.getLocation().clone();

        shooter.teleportAsync(hitLocation);
        hit.teleportAsync(shooterLocation);

        shooter.playSound(Sound.sound(Key.key("minecraft", "entity.player.teleport"), Sound.Source.PLAYER, 10f, 1f));
        hit.playSound(Sound.sound(Key.key("minecraft", "entity.player.teleport"), Sound.Source.PLAYER, 10f, 1f));
    }
}
