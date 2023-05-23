package net.nekozouneko.anniv2.listener;

import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.message.MessageManager;
import net.nekozouneko.anniv2.util.CmnUtil;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

        if (e.getEntity().getKiller() != null) {
            ChatColor cck = CmnUtil.getJoinedTeam(e.getEntity().getKiller()) != null ?
                    CmnUtil.getJoinedTeam(e.getEntity().getKiller()).getColor() : ChatColor.WHITE;
            e.setDeathMessage(mm.build("kill.default", cc + e.getEntity().getName(), cck + e.getEntity().getKiller().getName()));
        }
        else {
            switch (e.getEntity().getLastDamageCause().getCause()) {
                case FALL:
                    e.setDeathMessage(mm.build("kill.fall", cc + e.getEntity().getName()));
                    break;
                case VOID:
                    e.setDeathMessage(mm.build("kill.void", cc + e.getEntity().getName()));
                    break;
                case ENTITY_ATTACK:
                    e.setDeathMessage(mm.build("kill.mob", cc + e.getEntity().getName()));
                    break;
                default: {
                    e.setDeathMessage(mm.build("kill.game", cc + e.getEntity().getName()));
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
    }

}
