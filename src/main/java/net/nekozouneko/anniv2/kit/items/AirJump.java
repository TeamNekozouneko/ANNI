package net.nekozouneko.anniv2.kit.items;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.message.MessageManager;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class AirJump implements Listener {

    private static final Map<UUID, Long> COOLDOWN = new HashMap<>();

    public static void setCooldown(UUID target, Long end) {
        if (end != null) COOLDOWN.put(target, end);
        else COOLDOWN.remove(target);
    }

    public static Long getCooldown(UUID target) {
        return COOLDOWN.get(target);
    }

    public static boolean isCooldownEnd(UUID target) {
        Long res = COOLDOWN.get(target);
        if (res == null) return true;
        else return res <= System.currentTimeMillis();
    }

    public static ItemStackBuilder builder() {
        MessageManager mm = ANNIPlugin.getInstance().getMessageManager();
        return ItemStackBuilder.of(Material.FEATHER)
                .name(mm.build("item.airjump.name"))
                .lore(mm.buildList("item.airjump.lore"))
                .persistentData(
                        new NamespacedKey(ANNIPlugin.getInstance(), "special-item"),
                        PersistentDataType.STRING, "air-jump"
                );
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent e) {
        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getItem() != null) {
            PersistentDataContainer c = e.getItem().getItemMeta().getPersistentDataContainer();
            if (
                    c.getOrDefault(new NamespacedKey(ANNIPlugin.getInstance(), "special-item"), PersistentDataType.STRING, "")
                            .equals("air-jump")
            ) {
                e.setCancelled(true);
                if (isCooldownEnd(e.getPlayer().getUniqueId())) {
                    setCooldown(e.getPlayer().getUniqueId(), System.currentTimeMillis() + 10000);
                    e.getPlayer().getWorld().playSound(
                            e.getPlayer().getLocation(), Sound.ENTITY_WITHER_SHOOT, 1, 2
                    );
                    e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().setY(1));
                }
                else {
                    e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent(ANNIPlugin.getInstance().getMessageManager()
                                    .build("actionbar.cooldown_stats",
                                            getCooldown(e.getPlayer().getUniqueId()) == null ? "NaN" :
                                                    Objects.toString((getCooldown(e.getPlayer().getUniqueId()) - System.currentTimeMillis()) / 1000)
                                    )
                            )
                    );
                    e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 2);
                }
            }
        }
    }

}
