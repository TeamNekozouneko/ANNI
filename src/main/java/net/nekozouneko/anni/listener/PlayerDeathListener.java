package net.nekozouneko.anni.listener;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.kit.items.AirJump;
import net.nekozouneko.anni.message.MessageManager;
import net.nekozouneko.anni.util.CmnUtil;
import net.nekozouneko.anni.util.VaultUtil;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.LinkedHashSet;
import java.util.Objects;

public class PlayerDeathListener implements Listener {

    private final ANNIPlugin plugin = ANNIPlugin.getInstance();
    private final MessageManager mm = plugin.getMessageManager();

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        ChatColor cc = CmnUtil.getJoinedTeam(e.getEntity()) != null ?
                CmnUtil.getJoinedTeam(e.getEntity()).getColor() : ChatColor.WHITE;

        String deadName = cc + e.getEntity().getName() + "(" + plugin.getCurrentGame().getKit(e.getEntity()).getShortName() + ")";

        if (e.getEntity().getKiller() != null) {
            ChatColor cck = CmnUtil.getJoinedTeam(e.getEntity().getKiller()) != null ?
                    CmnUtil.getJoinedTeam(e.getEntity().getKiller()).getColor() : ChatColor.WHITE;
            String killerName = cck + e.getEntity().getKiller().getName() + "(" + plugin.getCurrentGame().getKit(e.getEntity().getKiller()).getShortName() + ")";

            e.setDeathMessage(mm.build("kill.default", deadName, killerName));

            VaultUtil.ifAvail((eco) -> {
                eco.depositPlayer(e.getEntity().getKiller(), 10);
                e.getEntity().getKiller().sendMessage(
                        mm.build("notify.deposit_points", "10", mm.build("gui.shop.full_ext"))
                );
            });
        }
        else {
            EntityDamageEvent ede = e.getEntity().getLastDamageCause();
            switch (ede != null ? ede.getCause() : EntityDamageEvent.DamageCause.VOID) {
                case FALL:
                    e.setDeathMessage(mm.build("kill.fall", deadName));
                    break;
                case VOID:
                    e.setDeathMessage(mm.build("kill.void", deadName));
                    break;
                case ENTITY_ATTACK:
                    e.setDeathMessage(mm.build("kill.mob", deadName));
                    break;
                default: {
                    e.setDeathMessage(mm.build("kill.game", deadName));
                    break;
                }
            }
        }

        // {kit-item: 1} じゃないアイテムをドロップさせる。
        new LinkedHashSet<>(e.getDrops()).stream()
                .filter(Objects::nonNull) // nullじゃないかつ
                .filter(is -> { // {kit-item:1}なら
                    PersistentDataContainer pdc = is.getItemMeta().getPersistentDataContainer();

                    return pdc.getOrDefault(
                            new NamespacedKey(ANNIPlugin.getInstance(), "kit-item"),
                            PersistentDataType.INTEGER, 0
                    ) == 1;
                })
                .forEach(e.getDrops()::remove); // ドロップするアイテムから削除

        AirJump.setCooldown(e.getEntity().getUniqueId(), null);
        PlayerDamageListener.setNotFighting(e.getEntity());
    }

}
