package net.nekozouneko.anni.item;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.message.TranslationManager;
import net.nekozouneko.anni.task.CooldownManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class GrapplingHook implements Listener {

    public static final List<EntityType> CANT_PULL_ENTITIES = Arrays.asList(
            EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.END_CRYSTAL,
            EntityType.AREA_EFFECT_CLOUD, EntityType.WARDEN, EntityType.IRON_GOLEM
    );

    public static ItemStack get(Locale locale) {
        TranslationManager tm = ANNIPlugin.getInstance().getTranslationManager();

        ItemStack item = ItemStack.of(Material.FISHING_ROD);
        item.editMeta(meta -> {
            meta.displayName(tm.component(locale, "item.grappling_hook.name"));
            meta.lore(tm.componentList(locale, "item.grappling_hook.lore"));
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            meta.getPersistentDataContainer().set(new NamespacedKey(ANNIPlugin.getInstance(), "special-item"), PersistentDataType.STRING, "grappling-hook");
        });

        return item;
    }

    @EventHandler
    public void onHook(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.FISHING) {
            processRodIsGrapplingHook(event);
            return;
        }

        PersistentDataContainer c = event.getHook().getPersistentDataContainer();

        if (c.getOrDefault(new NamespacedKey(ANNIPlugin.getInstance(), "grappling-hook"), PersistentDataType.INTEGER, 0) != 1)
            return;

        CooldownManager cm = ANNIPlugin.getInstance().getCooldownManager();

        switch (event.getState()) {
            case BITE: {
                event.setCancelled(true);
                break;
            }
            case IN_GROUND: {
                event.getPlayer().setVelocity(calculateVel(event.getPlayer().getLocation(), event.getHook().getLocation(), 2));
                cm.set(event.getPlayer().getUniqueId(), CooldownManager.Type.GRAPPLING_HOOK, 1000);
                break;
            }
            case CAUGHT_ENTITY: {
                if (!CANT_PULL_ENTITIES.contains(event.getCaught().getType())) {
                    event.getCaught().setVelocity(calculateVel(event.getCaught().getLocation(), event.getPlayer().getLocation(), 1));
                    cm.set(event.getPlayer().getUniqueId(), CooldownManager.Type.GRAPPLING_HOOK, 1000);
                }
                break;
            }
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (event.getEntity().getShooter() == null) return;
        if (!(event.getEntity() instanceof FishHook)) return;

        PersistentDataContainer c = event.getEntity().getPersistentDataContainer();

        if (c.getOrDefault(new NamespacedKey(ANNIPlugin.getInstance(), "grappling-hook"), PersistentDataType.INTEGER, 0) != 1)
            return;

        if (event.getHitEntity() != null) {
            event.setCancelled(true);
        }
    }

    private void processRodIsGrapplingHook(PlayerFishEvent event) {
        if (event.getHand() == null) return;

        ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
        if (item == null || item.getType().isAir()) return;

        if (isGrapplingHook(item)) {
            boolean cantUse = event.getPlayer().isVisualFire();
            cantUse = cantUse || event.getPlayer().hasPotionEffect(PotionEffectType.BLINDNESS);
            cantUse = cantUse || event.getPlayer().hasPotionEffect(PotionEffectType.DARKNESS);
            cantUse = cantUse || event.getPlayer().hasPotionEffect(PotionEffectType.LEVITATION);

            try {
                cantUse = cantUse || event.getPlayer().isFrozen();
            }
            catch (Exception ignored) {}

            if (cantUse) {
                event.setCancelled(true);
                return;
            }

            CooldownManager cm = ANNIPlugin.getInstance().getCooldownManager();

            if (!cm.isCooldownEnd(event.getPlayer().getUniqueId(), CooldownManager.Type.GRAPPLING_HOOK)) {
                event.setCancelled(true);

                event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent(
                                ANNIPlugin.getInstance().getMessageManager().build(
                                    "command.err.cooldown",
                                        cm.getTimeLeftFormatted(event.getPlayer().getUniqueId(), CooldownManager.Type.GRAPPLING_HOOK)

                                )
                        )
                );
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 2);
                return;
            }

            event.getHook().getPersistentDataContainer()
                    .set(new NamespacedKey(ANNIPlugin.getInstance(), "grappling-hook"), PersistentDataType.INTEGER, 1);
            event.getHook().setVelocity(event.getHook().getVelocity().multiply(1.75));
        }
    }

    private Vector calculateVel(Location from, Location to, double m) {
        Vector vel = to.subtract(from).toVector();

        return vel.normalize().multiply(m);
    }

    public static boolean isGrapplingHook(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;

        return item.getItemMeta().getPersistentDataContainer().getOrDefault(
                new NamespacedKey(ANNIPlugin.getInstance(), "special-item"),
                PersistentDataType.STRING, ""
        ).equals("grappling-hook");
    }

}
