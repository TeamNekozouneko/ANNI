package net.nekozouneko.anni.listener.votifier;

import com.vexsoftware.votifier.model.VotifierEvent;
import net.nekozouneko.anni.ANNIConfig;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.message.MessageManager;
import net.nekozouneko.anni.util.VaultUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class VotifierListener implements Listener {
    
    private final MessageManager mm = ANNIPlugin.getInstance().getMessageManager();

    @EventHandler
    public void onVote(VotifierEvent e) {
        if (!ANNIConfig.isVotifierVoteEnabled()) return;

        OfflinePlayer op = null;
        for (OfflinePlayer off : Bukkit.getOfflinePlayers()) {
            if (e.getVote().getUsername().equals(off.getName())) {
                op = off;
                break;
            }
        }
        if (op == null) {
            Bukkit.getLogger().info("Failed vote. by " + e.getVote().getUsername());
            return;
        }
        
        Bukkit.broadcastMessage(mm.buildLines(
                "notify.voted", e.getVote().getUsername(), e.getVote().getServiceName()
        ));

        if (VaultUtil.hasEco()) {
            VaultUtil.getEco().depositPlayer(op, ANNIConfig.getVotePoints());
        }
        if (op.isOnline()) {
            ((Player) op).sendMessage(mm.build(
                    "notify.thank_you_voting",
                    Double.toString(ANNIConfig.getVotePoints()),
                    mm.build("gui.shop.ext")
            ));
        }
    }

}
