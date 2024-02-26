package net.nekozouneko.anni.listener;

import com.google.common.base.Enums;
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

import java.util.*;

public final class BlockPlaceListener implements Listener {

    public static final List<Material> BLACKLIST;

    static {
        List<Material> black = new ArrayList<>(Arrays.asList(
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
        ));

        if (Enums.getIfPresent(Material.class, "DEEPSLATE").isPresent()) {
            black.addAll(Arrays.asList(
                    Material.DEEPSLATE_COAL_ORE,
                    Material.DEEPSLATE_COPPER_ORE,
                    Material.DEEPSLATE_DIAMOND_ORE,
                    Material.DEEPSLATE_EMERALD_ORE,
                    Material.DEEPSLATE_GOLD_ORE,
                    Material.DEEPSLATE_IRON_ORE,
                    Material.DEEPSLATE_LAPIS_ORE,
                    Material.DEEPSLATE_REDSTONE_ORE,
                    Material.COPPER_ORE
            ));
        }

        black.add(Material.MELON);
        black.add(Material.GRAVEL);

        black.addAll(Arrays.asList( // 原木
                Material.ACACIA_LOG, Material.BIRCH_LOG, Material.DARK_OAK_LOG,
                Material.JUNGLE_LOG, Material.OAK_LOG, Material.SPRUCE_LOG,
                Material.CRIMSON_STEM, Material.WARPED_STEM,
                Material.STRIPPED_ACACIA_LOG, Material.STRIPPED_BIRCH_LOG,
                Material.STRIPPED_DARK_OAK_LOG, Material.STRIPPED_JUNGLE_LOG,
                Material.STRIPPED_OAK_LOG, Material.STRIPPED_SPRUCE_LOG,
                Material.STRIPPED_CRIMSON_STEM, Material.STRIPPED_WARPED_STEM
        ));

        black.add(Material.LAVA);
        black.add(Material.LAVA_BUCKET);

        BLACKLIST = Collections.unmodifiableList(black);
    }

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
