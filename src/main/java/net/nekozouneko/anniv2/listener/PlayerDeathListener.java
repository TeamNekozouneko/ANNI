package net.nekozouneko.anniv2.listener;

import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.message.MessageManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final ANNIPlugin plugin = ANNIPlugin.getInstance();
    private final MessageManager mm = plugin.getMessageManager();

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (e.getEntity().getKiller() != null) {
            e.setDeathMessage(mm.build("kill.default", e.getEntity().getName(), e.getEntity().getKiller().getName()));
        }
        else {
            switch (e.getEntity().getLastDamageCause().getCause()) {
                case FALL:
                    e.setDeathMessage(mm.build("kill.fall", e.getEntity().getName()));
                    break;
                case VOID:
                    e.setDeathMessage(mm.build("kill.void", e.getEntity().getName()));
                    break;
                case ENTITY_ATTACK:
                    e.setDeathMessage(mm.build("kill.mob", e.getEntity().getName()));
                    break;
                default: {
                    e.setDeathMessage(mm.build("kill.game", e.getEntity().getName()));
                    break;
                }
            }
        }
    }

}
