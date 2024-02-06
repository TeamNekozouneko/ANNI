package net.nekozouneko.anni.kit.items;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.ANNIArena;
import net.nekozouneko.anni.arena.spectator.SpectatorManager;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DefenseArtifact implements Listener {

    private static final Map<UUID, EffectTask> TASKS = new HashMap<>();
    private static final Map<UUID, Long> COOLDOWN = new HashMap<>();

    public static class EffectTask extends BukkitRunnable {

        public EffectTask(Player player, int time, int radius) {
            this.player = player;
            this.for_first_check = time;
            this.time = time;
            this.radius = radius;
        }

        private final Player player;
        private final int for_first_check;
        private int time;
        private final int radius;

        @Override
        public void run() {
            if (player.getPlayer() == null || time < 0) {
                new HashSet<>(TASKS.keySet()).forEach(key -> {
                    if (Objects.equals(TASKS.get(key), this)) TASKS.remove(key);
                });

                cancel();
                return;
            }

            if (for_first_check == time) {
                player.getWorld().spawnParticle(
                        Particle.DRAGON_BREATH, player.getLocation(),
                        1000, .1, .1, .1, 1
                );
                player.getWorld().playSound(
                        player.getLocation(), Sound.BLOCK_ANVIL_USE, 2, 0
                );
            }

            ANNIArena game = ANNIPlugin.getInstance().getCurrentGame();
            ANNIPlugin.getInstance().getCurrentGame().getPlayers().stream()
                    .filter(p -> !SpectatorManager.isSpectating(p))
                    .filter(p -> game.getTeamByPlayer(p) != null)
                    .filter(p -> game.getTeamByPlayer(p) != game.getTeamByPlayer(player))
                    .filter(p -> isInCylinder(player.getLocation(), p.getLocation()))
                    .forEach(victim -> {
                        if (for_first_check == time) {
                            victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 0, false, true, true));
                            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 1, false, true, true));
                            GrapplingHook.addCooldown(victim.getUniqueId(), 5000);
                        }

                        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1, false, true, true));
                    });

            time--;
        }

        private boolean isInCylinder(Location center, Location pos) {
            boolean isPass = center.getY()+2.5 >= pos.getY() && pos.getY() >= center.getY()-2.5;

            isPass = isPass && radius >= Math.sqrt(Math.pow(pos.getX() - center.getX(), 2) + Math.pow(pos.getZ() - center.getZ(), 2));

            return isPass;
        }
    }

    public static ItemStackBuilder builder() {
        return ItemStackBuilder.of(Material.HEART_OF_THE_SEA)
                .name(ANNIPlugin.getInstance().getMessageManager().build("item.defense_artifact.name"))
                .lore(ANNIPlugin.getInstance().getMessageManager().buildList("item.defense_artifact.lore"))
                .persistentData(new NamespacedKey(ANNIPlugin.getInstance(), "special-item"), PersistentDataType.STRING, "defense-artifact")
                .enchant(Enchantment.DURABILITY, 1, false)
                .itemFlags(ItemFlag.HIDE_ENCHANTS);
    }

    public static void cancelTask(UUID player) {
        if (TASKS.get(player) == null) return;

        EffectTask task = TASKS.remove(player);
        if (task.isCancelled()) return;

        task.cancel();
    }

    public static void cancelAllTasks() {
        TASKS.values().forEach(task -> {
            if (task.isCancelled()) return;

            try {
                task.cancel();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
        TASKS.clear();
    }

    public static long getCooldown(UUID player) {
        return COOLDOWN.getOrDefault(player, System.currentTimeMillis());
    }

    public static void addCooldown(UUID player, long time) {
        COOLDOWN.put(player, System.currentTimeMillis() + time);
    }

    public static boolean isCooldownEnd(UUID player) {
        return getCooldown(player) <= System.currentTimeMillis();
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;

        if (event.getItem() == null || event.getItem().getType().isAir()) return;

        PersistentDataContainer c = event.getItem().getItemMeta().getPersistentDataContainer();

        if (!c.getOrDefault(new NamespacedKey(ANNIPlugin.getInstance(), "special-item"), PersistentDataType.STRING, "").equals("defense-artifact")) return;

        event.setCancelled(true);

        if (!isCooldownEnd(event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage(ANNIPlugin.getInstance().getMessageManager().build(
                    "command.err.cooldown", (getCooldown(event.getPlayer().getUniqueId()) - System.currentTimeMillis()) / 1000
            ));
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 2);
            return;
        }

        EffectTask task = new EffectTask(event.getPlayer(), 10, 10);
        task.runTaskTimer(ANNIPlugin.getInstance(), 0, 20);
        TASKS.put(event.getPlayer().getUniqueId(), task);
        addCooldown(event.getPlayer().getUniqueId(), 60000);
    }

}
