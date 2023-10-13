package net.nekozouneko.anni.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.ANNIArena;
import net.nekozouneko.anni.arena.team.ANNITeam;
import net.nekozouneko.anni.map.Nexus;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class BlockPlaceListener implements Listener {

    public static final List<Material> BLACKLIST = Arrays.asList(
            // 鉱石
            Material.COAL_ORE,
            Material.DIAMOND_ORE,
            Material.EMERALD_ORE,
            Material.GOLD_ORE,
            Material.IRON_ORE,
            Material.LAPIS_ORE,
            Material.NETHER_GOLD_ORE,
            Material.NETHER_QUARTZ_ORE,
            Material.REDSTONE_ORE,
            Material.GRAVEL,
            Material.LAVA,
            Material.LAVA_BUCKET
    );

    private final ANNIPlugin plugin = ANNIPlugin.getInstance();

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        ANNIArena current = plugin.getCurrentGame();

        if (current.getState().getId() > 0 && current.isJoined(p)) {
            // ゲーム中に置かないでほしいブロックをおけなくする
            if (p.getGameMode() != GameMode.CREATIVE && BLACKLIST.contains(e.getItemInHand().getType()))
                e.setCancelled(true);
            else {
                // copyWorldがあるなら
                if (current.getCopyWorld() != null) {
                    // 有効なチームのネクサスでforループ
                    for (Map.Entry<ANNITeam, Nexus> ent : current.getMap().getNexuses().entrySet()) {
                        Location loc = BukkitAdapter.adapt(
                                current.getCopyWorld(),
                                ent.getValue().getLocation()
                        );
                        // もしネクサスと置かれたブロックの位置が同じなら
                        if (e.getBlock().getLocation().equals(loc)) {
                            e.setCancelled(true); // キャンセルして
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                if (current.isNexusLost(ent.getKey())) // ネクサスの体力が0もしくはnullの場合
                                    e.getBlock().setType(Material.BEDROCK); // 岩盤設置
                                else e.getBlock().setType(Material.END_STONE); // そうじゃないならエンドストーンを設置
                            });
                            return;
                        }
                    }
                }
            }
        }
    }

}
