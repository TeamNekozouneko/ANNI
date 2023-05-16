package net.nekozouneko.anniv2.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.arena.ANNIArena;
import net.nekozouneko.anniv2.arena.team.ANNITeam;
import net.nekozouneko.anniv2.map.Nexus;
import net.nekozouneko.anniv2.util.CmnUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class BlockBreakListener implements Listener {

    private static final Map<UUID, Consumer<Block>> queuedOnDamage = new HashMap<>();
    private static final Map<Material, Long> cooldown = new EnumMap<>(Material.class);

    public static Map<UUID, Consumer<Block>> getQueuedOnDamageMap() {
        return queuedOnDamage;
    }

    static {
        cooldown.put(Material.COAL_ORE, 100L);
        cooldown.put(Material.DIAMOND_ORE, 600L);
        cooldown.put(Material.EMERALD_ORE, 600L);
        cooldown.put(Material.GOLD_ORE, 400L);
        cooldown.put(Material.IRON_ORE, 200L);
        cooldown.put(Material.LAPIS_ORE, 200L);
        cooldown.put(Material.NETHER_GOLD_ORE, 400L);
        cooldown.put(Material.NETHER_QUARTZ_ORE, 200L);
        cooldown.put(Material.REDSTONE_ORE, 200L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBreak(BlockBreakEvent e) {
        Consumer<Block> cb = queuedOnDamage.get(e.getPlayer().getUniqueId());
        if (cb != null) {
            cb.accept(e.getBlock());
            queuedOnDamage.remove(e.getPlayer().getUniqueId());
            e.setCancelled(true);
            return;
        }

        ANNIPlugin plugin = ANNIPlugin.getInstance();
        ANNIArena current = plugin.getCurrentGame();

        if (current.getState().getId() > 0) {
            // ネクサス
            for (Map.Entry<ANNITeam, Nexus> ent : current.getMap().getNexuses().entrySet()) {
                if (!current.getTeams().containsKey(ent.getKey())) continue;
                Location loc = BukkitAdapter.adapt(
                        current.getCopyWorld(),
                        ent.getValue().getLocation()
                );
                if (e.getBlock().getLocation().equals(loc)) {
                    e.setDropItems(false);
                    e.setExpToDrop(0);
                    if (current.isNexusLost(ent.getKey())) {
                        e.setCancelled(true);
                        return;
                    }

                    // 破壊しようとしてるのは自チームかどうか
                    if (current.getTeamByPlayer(e.getPlayer()).equals(ent.getKey())) {
                        e.getPlayer().sendMessage(plugin.getMessageManager().build("nexus.cant_destroy_self"));
                        e.setCancelled(true);
                    }
                    else if (current.getState().canDestroyNexus()) { // 現在のフェーズで破壊できるなら
                        current.damageNexusHealth(ent.getKey(), 1, e.getPlayer());
                        if (current.isNexusLost(ent.getKey())) {
                            Nexus.finalDestroyEffects(loc);
                            Bukkit.getScheduler().runTask(plugin, () -> e.getBlock().setType(Material.BEDROCK));
                        } else {
                            Nexus.destroyEffects(loc);
                            Bukkit.getScheduler().runTaskLater(plugin, () -> e.getBlock().setType(Material.END_STONE), 3);
                        }
                    }
                    else { // 現在のフェーズで破壊できないなら
                        e.getPlayer().sendMessage(plugin.getMessageManager().build("nexus.now_cant_destroy"));
                        e.setCancelled(true);
                    }

                    return;
                }
            }

            if (cooldown.containsKey(e.getBlock().getType()) && e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                ItemStack mainHand = e.getPlayer().getInventory().getItemInMainHand();
                if (e.getBlock().getDrops(mainHand).isEmpty()) {
                    e.setCancelled(true);
                    return;
                }
                CmnUtil.giveOrDrop(
                        e.getPlayer(),
                        e.getBlock().getDrops(mainHand).toArray(new ItemStack[0])
                );

                e.setDropItems(false);
                e.getPlayer().giveExp(e.getExpToDrop());
                e.setExpToDrop(0);

                BlockData cloned = e.getBlock().getBlockData().clone(); // ブロックデータを複製
                Material typ = e.getBlock().getType();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    e.getBlock().setType(Material.COBBLESTONE);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        e.getBlock().setType(typ); // クールダウン終了後再生成
                        e.getBlock().setBlockData(cloned); // 複製したデータに変更
                    }, cooldown.get(typ) - 1);
                });

                return;
            }
        }
    }

}
