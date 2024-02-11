package net.nekozouneko.anni.listener;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.spectator.SpectatorManager;
import net.nekozouneko.anni.kit.ANNIKit;
import net.nekozouneko.anni.kit.items.GrapplingHook;
import net.nekozouneko.anni.message.MessageManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class PlayerDamageListener implements Listener {

    private static final Map<UUID, Long> fighting = new HashMap<>();
    private static final List<Material> AXES = Arrays.asList(
            Material.STONE_AXE, Material.DIAMOND_AXE,
            Material.WOODEN_AXE, Material.STONE_AXE,
            Material.NETHERITE_AXE, Material.IRON_AXE
    );

    private static final List<EntityDamageEvent.DamageCause> DIRECT_ATTACK_CAUSES = Arrays.asList(
            EntityDamageEvent.DamageCause.ENTITY_ATTACK,
            EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK,
            EntityDamageEvent.DamageCause.THORNS
    );

    public static boolean isFighting(Player player) {
        new HashSet<>(fighting.keySet()).forEach(key -> {
            if (fighting.getOrDefault(key, 0L) <= System.currentTimeMillis())
                fighting.remove(key);
        });

        return !(fighting.getOrDefault(player.getUniqueId(), 0L) <= System.currentTimeMillis());
    }

    public static void setNotFighting(Player player) {
        fighting.remove(player.getUniqueId());
    }

    private final ANNIPlugin plugin = ANNIPlugin.getInstance();
    private final MessageManager mm = plugin.getMessageManager();

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = ((Player) e.getEntity());

            if (SpectatorManager.isSpectating(p)) {
                e.setCancelled(true);
                return;
            }

            if (ANNIPlugin.getInstance().getCurrentGame().getState().getId() >= 0) {
                switch (ANNIKit.get(ANNIPlugin.getInstance().getCurrentGame().getKit(p))) {
                    case ACROBAT: {
                        if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                            e.setCancelled(true);
                            return;
                        }
                        break;
                    }
                    case SCOUTER: {
                        if (e.getCause() != EntityDamageEvent.DamageCause.FALL) {
                            GrapplingHook.addCooldown(p.getUniqueId(), 5000);
                        }
                        break;
                    }
                }

                if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    p.removePotionEffect(PotionEffectType.INVISIBILITY);
                    p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 2);
                    p.sendMessage(mm.build("notify.removed_invisibility"));
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player damager = (Player) e.getDamager();
            ItemStack main = damager.getInventory().getItemInMainHand();
            // 斧で攻撃した場合
            if (AXES.contains(main.getType())) {
                // 4ダメージより上なら4ダメージを減らして0.4 * ダメージ増加 (ない場合0)のレベル減らしてそうではないならそのまま通す
                e.setDamage(Math.max(e.getDamage() > 2 ? e.getDamage() - 4 - (0.5 * main.getEnchantmentLevel(Enchantment.DAMAGE_ALL)) : e.getDamage(), 0));
            }

            if (ANNIKit.get(ANNIPlugin.getInstance().getCurrentGame().getKit(damager)) == ANNIKit.VAMPIRE) {
                if (DIRECT_ATTACK_CAUSES.contains(e.getCause()) && new Random().nextDouble() >= 0.60) {
                    damager.getWorld().playSound(e.getEntity().getLocation(), Sound.BLOCK_HONEY_BLOCK_SLIDE, 1, 0);

                    Location particlePos = e.getEntity().getLocation().clone();
                    particlePos.setY(particlePos.getY() + 1);

                    e.getEntity().getWorld().spawnParticle(
                            Particle.BLOCK_DUST, particlePos,
                            100, .1, .25, .1, 1,
                            Material.REDSTONE_BLOCK.createBlockData()
                    );
                    damager.setHealth(Math.min(
                            damager.getHealth() + Math.min((e.getDamage() * ((double) (new Random().nextInt(25) + 10) / 100)), 3),
                            damager.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null ? damager.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() : 20D
                    ));
                }
            }

            if (e.getEntity() instanceof Player) {
                fighting.put(e.getEntity().getUniqueId(), System.currentTimeMillis() + 10000);
            }
        }
    }

}
