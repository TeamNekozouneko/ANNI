package net.nekozouneko.anni.item;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.message.MessageManager;
import net.nekozouneko.anni.task.CooldownManager;
import net.nekozouneko.anni.task.RechargeManager;
import net.nekozouneko.anni.util.CmnUtil;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataHolder;
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
                .enchant(Enchantment.UNBREAKING, 1, false);
    }

    public static boolean isStunGrenade(PersistentDataHolder holder) {
        if (holder == null) return false;

        NamespacedKey key = new NamespacedKey(ANNIPlugin.getInstance(), "special-item");

        return holder.getPersistentDataContainer()
                .getOrDefault(key, PersistentDataType.STRING, "").equals("stun-grenade");
    }

    @EventHandler(ignoreCancelled = true)
    public void onLaunch(PlayerLaunchProjectileEvent e) {
        if (!isStunGrenade(e.getItemStack().getItemMeta())) return;

        if (e.getItemStack().getAmount() == 1) {
            e.setCancelled(true);

            ANNIPlugin plugin = ANNIPlugin.getInstance();
            if (plugin.getCurrentGame().getRechargeManager() != null) {
                e.getPlayer().sendActionBar(LegacyComponentSerializer.legacyAmpersand().deserialize(
                        plugin.getMessageManager().build("actionbar.cooldown_recharge",
                                plugin.getCurrentGame().getRechargeManager().getTimeLeftFormatted(e.getPlayer(), RechargeManager.Type.STUN_GRENADE)
                        )
                ));
            }
            return;
        }

        NamespacedKey siKey = new NamespacedKey(ANNIPlugin.getInstance(), "special-item");
        e.getProjectile().getPersistentDataContainer()
                .set(siKey, PersistentDataType.STRING, "stun-grenade");

        new BukkitRunnable() {
            @Override
            public void run() {
                if (e.getProjectile().isDead()) cancel();
                else {

                    e.getProjectile().getWorld().spawnParticle(
                            Particle.BLOCK, e.getProjectile().getLocation(),
                            5, 0, 0, 0, Material.PLAYER_HEAD.createBlockData()
                    );
                }
            }
        }.runTaskTimer(ANNIPlugin.getInstance(), 0, 1);
    }

    @EventHandler
    public void onHit(ProjectileHitEvent e) {
        if (isStunGrenade(e.getEntity())) {
            if (e.getHitEntity() != null && e.getHitEntity() instanceof Player hit) {
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

                    hit.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 2));
                    hit.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 0));

                    CooldownManager cm = ANNIPlugin.getInstance().getCooldownManager();
                    cm.set(hit.getUniqueId(), CooldownManager.Type.AIR_JUMP, 10000);
                    cm.set(hit.getUniqueId(), CooldownManager.Type.GRAPPLING_HOOK, 15000);
                }
            }
        }
    }
}