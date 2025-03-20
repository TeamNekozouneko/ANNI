package net.nekozouneko.anni.item;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.ANNIArena;
import net.nekozouneko.anni.arena.spectator.SpectatorManager;
import net.nekozouneko.anni.task.CooldownManager;
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

            ANNIArena game = ANNIPlugin.getInstance().getCurrentGame();
            String region = game.getMap().getTeamRegion(game.getTeamByPlayer(player));

            if (region != null) {
                ProtectedRegion pr = WorldGuard.getInstance().getPlatform().getRegionContainer()
                        .get(BukkitAdapter.adapt(game.getCopyWorld()))
                        .getRegion(region);

                if (!pr.contains(BukkitAdapter.asBlockVector(player.getLocation()))) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                            ANNIPlugin.getInstance().getMessageManager().build(
                                    "actionbar.out_of_team_region"
                            )
                    ));
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 0);

                    cancel();
                    new HashSet<>(TASKS.keySet()).forEach(key -> {
                        if (Objects.equals(TASKS.get(key), this)) TASKS.remove(key);
                    });
                    return;
                }
            }

            if (for_first_check == time) {
                player.getWorld().spawnParticle(
                        Particle.DRAGON_BREATH, player.getLocation(),
                        1000, .1, .1, .1, 1
                );
                player.getWorld().playSound(
                        player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 0
                );
            }

            ANNIPlugin.getInstance().getCurrentGame().getPlayers().stream()
                    .filter(p -> !SpectatorManager.isSpectating(p))
                    .filter(p -> game.getTeamByPlayer(p) != null)
                    .filter(p -> game.getTeamByPlayer(p) != game.getTeamByPlayer(player))
                    .filter(p -> isInCylinder(player.getLocation(), p.getLocation()))
                    .forEach(victim -> {
                        if (for_first_check == time) {
                            victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 0, false, true, true));
                            victim.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 254, false, true, true));
                            ANNIPlugin.getInstance().getCooldownManager().set(victim.getUniqueId(), CooldownManager.Type.GRAPPLING_HOOK, 5000);
                        }

                        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1, false, true, true));
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
                .enchant(Enchantment.UNBREAKING, 1, false)
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

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;

        if (event.getItem() == null || event.getItem().getType().isAir()) return;

        PersistentDataContainer c = event.getItem().getItemMeta().getPersistentDataContainer();

        if (!c.getOrDefault(new NamespacedKey(ANNIPlugin.getInstance(), "special-item"), PersistentDataType.STRING, "").equals("defense-artifact")) return;

        event.setCancelled(true);

        CooldownManager cm = ANNIPlugin.getInstance().getCooldownManager();

        if (!cm.isCooldownEnd(event.getPlayer().getUniqueId(), CooldownManager.Type.DEFENSE_ARTIFACT)) {
            event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ANNIPlugin.getInstance().getMessageManager().build(
                    "command.err.cooldown", cm.getTimeLeftFormatted(event.getPlayer().getUniqueId(), CooldownManager.Type.DEFENSE_ARTIFACT)
            )));
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 2);
            return;
        }

        ANNIArena game = ANNIPlugin.getInstance().getCurrentGame();
        String region = game.getMap().getTeamRegion(game.getTeamByPlayer(event.getPlayer()));

        if (region != null) {
            ProtectedRegion pr = WorldGuard.getInstance().getPlatform().getRegionContainer()
                    .get(BukkitAdapter.adapt(game.getCopyWorld()))
                    .getRegion(region);

            if (!pr.contains(BukkitAdapter.asBlockVector(event.getPlayer().getLocation()))) {
                event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                        ANNIPlugin.getInstance().getMessageManager().build(
                                "actionbar.out_of_team_region"
                        )
                ));
                return;
            }
        }

        EffectTask task = new EffectTask(event.getPlayer(), 10, 10);
        task.runTaskTimer(ANNIPlugin.getInstance(), 0, 20);
        TASKS.put(event.getPlayer().getUniqueId(), task);
        cm.set(event.getPlayer().getUniqueId(), CooldownManager.Type.DEFENSE_ARTIFACT, 60000);
    }

}
