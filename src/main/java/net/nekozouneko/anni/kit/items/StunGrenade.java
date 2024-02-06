package net.nekozouneko.anni.kit.items;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.message.MessageManager;
import net.nekozouneko.anni.util.CmnUtil;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.atomic.AtomicBoolean;

public class StunGrenade implements Listener {

    public static ItemStackBuilder builder() {
        MessageManager mm = ANNIPlugin.getInstance().getMessageManager();

        return ItemStackBuilder.of(Material.SNOWBALL)
                .name(mm.build("item.stun_grenade.name"))
                .lore(mm.buildList("item.stun_grenade.lore"))
                .persistentData(
                        new NamespacedKey(ANNIPlugin.getInstance(), "special-item"),
                        PersistentDataType.STRING, "stun-grenade"
                )
                .itemFlags(ItemFlag.HIDE_ENCHANTS)
                .enchant(Enchantment.DURABILITY, 1, false);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onLaunch(ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() != null && e.getEntity().getShooter() instanceof Player) {
            Player s = ((Player) e.getEntity().getShooter());
            if (s.getInventory().getItemInMainHand() == null || s.getInventory().getItemInMainHand().getType().isAir()) return;
            PersistentDataContainer pdc = s.getInventory().getItemInMainHand()
                    .getItemMeta().getPersistentDataContainer();
            NamespacedKey siKey = new NamespacedKey(ANNIPlugin.getInstance(), "special-item");

            if (pdc.has(siKey, PersistentDataType.STRING)) {
                if (pdc.get(siKey, PersistentDataType.STRING).equals("stun-grenade")) {
                    e.getEntity().getPersistentDataContainer()
                            .set(siKey, PersistentDataType.STRING, "stun-grenade");
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (e.getEntity().isDead()) cancel();
                            else {

                                e.getEntity().getWorld().spawnParticle(
                                        Particle.BLOCK_DUST, e.getEntity().getLocation(),
                                        5, 0, 0, 0, Material.PLAYER_HEAD.createBlockData()
                                );
                            }
                        }
                    }.runTaskTimer(ANNIPlugin.getInstance(), 0, 1);
                }
            }
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent e) {
        if (e.getEntity().getPersistentDataContainer().has(
                new NamespacedKey(ANNIPlugin.getInstance(), "special-item"), PersistentDataType.STRING
        ) && e.getEntity().getPersistentDataContainer().get(
                new NamespacedKey(ANNIPlugin.getInstance(), "special-item"), PersistentDataType.STRING)
                .equals("stun-grenade")
        ) {
            if (e.getHitEntity() != null && e.getHitEntity() instanceof Player) {
                Player hit = ((Player) e.getHitEntity());

                if (!(e.getEntity().getShooter() instanceof Player) || CmnUtil.canDamage((Player) e.getEntity().getShooter(), hit)) {
                    hit.damage(1, e.getEntity());

                    hit.setCooldown(Material.SHIELD, 200);
                    if (hit.isBlocking()) {
                        hit.playSound(hit.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);

                        ItemStack shield;
                        AtomicBoolean isOff = new AtomicBoolean(false);
                        PlayerInventory pi = hit.getInventory();
                        if (pi.getItemInMainHand().getType() == Material.SHIELD && pi.getItemInOffHand().getType() == Material.SHIELD) {
                            shield = pi.getItemInMainHand();
                            pi.setItemInMainHand(null);
                        } else if (pi.getItemInMainHand().getType() == Material.SHIELD) {
                            shield = pi.getItemInMainHand();
                            pi.setItemInMainHand(null);
                        } else if (pi.getItemInOffHand().getType() == Material.SHIELD) {
                            shield = pi.getItemInOffHand();
                            pi.setItemInOffHand(null);
                            isOff.set(true);
                        } else {
                            shield = null;
                        }

                        if (shield != null) {
                            Bukkit.getScheduler().runTask(ANNIPlugin.getInstance(), () -> {
                                if (isOff.get()) {
                                    if (!pi.getItemInOffHand().getType().isAir()) {
                                        CmnUtil.giveOrDrop(hit, shield);
                                    } else pi.setItemInOffHand(shield);
                                } else {
                                    if (!pi.getItemInMainHand().getType().isAir()) {
                                        CmnUtil.giveOrDrop(hit, shield);
                                    } else pi.setItemInMainHand(shield);
                                }
                            });
                        }
                    }

                    hit.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 2));
                    hit.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1));
                    GrapplingHook.addCooldown(hit.getUniqueId(), 5000);
                }
            }
        }
    }


}
