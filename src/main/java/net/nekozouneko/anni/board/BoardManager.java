package net.nekozouneko.anni.board;

import fr.mrmicky.fastboard.FastBoard;
import net.nekozouneko.anni.ANNIPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class BoardManager implements Listener {

    public class ANNIFastBoard extends FastBoard {

        public ANNIFastBoard(Player player) {
            super(player);
        }

        @Override
        public boolean hasLinesMaxLength() {
            try {
                Class<?> via = Class.forName("com.viaversion.viaversion.api.Via");
                Method getApi = via.getMethod("getAPI"); // com.viaversion.viaversion.api.Via#getAPI()
                Object viaApi = getApi.invoke(null);

                // com.viaversion.viaversion.api.Via#getAPI()#getPlayerVersion(Player)
                Method getpv = viaApi.getClass().getMethod("getPlayerVersion", Player.class);

                // com.viaversion.viaversion.api.protocol.version.ProtocolVersion.V1_13#getVersion()
                Class<?> prov = Class.forName("com.viaversion.viaversion.api.protocol.version.ProtocolVersion");
                Field v1_13Field = prov.getField("v1_13");
                Object v1_13inst = v1_13Field.get(null);
                Method getVer = v1_13inst.getClass().getMethod("getVersion");

                return (int) getpv.invoke(viaApi, getPlayer()) < (int) getVer.invoke(v1_13inst);
            }
            catch (Exception ignored) {}

            return super.hasLinesMaxLength();
        }

    }

    private final ANNIPlugin plugin;

    private final Map<Player, ANNIFastBoard> boards = new HashMap<>();

    public BoardManager(ANNIPlugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public ANNIFastBoard create(Player player) {
        delete(player);
        return boards.put(player, new ANNIFastBoard(player));
    }

    public ANNIFastBoard get(Player player) {
        return boards.getOrDefault(player, new ANNIFastBoard(player));
    }

    public void delete(Player player) {
        FastBoard fb = boards.remove(player);
        if (fb != null && !fb.isDeleted()) {
            fb.delete();
        }
    }

    public boolean has(Player player) {
        return boards.get(player) != null;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        delete(e.getPlayer());
    }
}
