package net.nekozouneko.anni.point;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.util.VaultUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PointManager {

    public void givePoint(Player player, long point) {
        ANNIPlugin.getInstance().getLevelManager().giveExp(player, point);
        VaultUtil.getEco().depositPlayer(player, point);
    }

    public long getPoint(OfflinePlayer player) {
        return VaultUtil.getEco().hasAccount(player) ? (long) VaultUtil.getEco().getBalance(player) : 0;
    }
}
