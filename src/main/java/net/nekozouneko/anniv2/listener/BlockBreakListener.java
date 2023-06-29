package net.nekozouneko.anniv2.listener;

import com.google.common.base.Enums;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.arena.ANNIArena;
import net.nekozouneko.anniv2.arena.team.ANNITeam;
import net.nekozouneko.anniv2.map.Nexus;
import net.nekozouneko.anniv2.util.CmnUtil;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;

public class BlockBreakListener implements Listener {

    private static final Map<UUID, Consumer<Block>> QUEUED_ON_DAMAGE = new HashMap<>();
    private static final Map<Material, Long> COOLDOWN = new EnumMap<>(Material.class);
    private static final Map<Material, Long> NO_BLOCK_COOLDOWN = new EnumMap<>(Material.class);
    private static final List<Material> WOODS = new ArrayList<>(Arrays.asList(
            Material.ACACIA_LOG, Material.BIRCH_LOG, Material.DARK_OAK_LOG,
            Material.JUNGLE_LOG, Material.OAK_LOG, Material.SPRUCE_LOG,
            Material.CRIMSON_STEM, Material.WARPED_STEM,
            Material.STRIPPED_ACACIA_LOG, Material.STRIPPED_BIRCH_LOG,
            Material.STRIPPED_DARK_OAK_LOG, Material.STRIPPED_JUNGLE_LOG,
            Material.STRIPPED_OAK_LOG, Material.STRIPPED_SPRUCE_LOG,
            Material.STRIPPED_CRIMSON_STEM, Material.STRIPPED_WARPED_STEM
    ));

    public static Map<UUID, Consumer<Block>> getQueuedOnDamageMap() {
        return QUEUED_ON_DAMAGE;
    }

    static {
        COOLDOWN.put(Material.COAL_ORE, 100L);
        COOLDOWN.put(Material.DIAMOND_ORE, 600L);
        COOLDOWN.put(Material.EMERALD_ORE, 600L);
        COOLDOWN.put(Material.GOLD_ORE, 400L);
        COOLDOWN.put(Material.GRAVEL, 100L);
        COOLDOWN.put(Material.IRON_ORE, 200L);
        COOLDOWN.put(Material.LAPIS_ORE, 200L);
        COOLDOWN.put(Material.NETHER_GOLD_ORE, 400L);
        COOLDOWN.put(Material.NETHER_QUARTZ_ORE, 200L);
        COOLDOWN.put(Material.REDSTONE_ORE, 200L);
        COOLDOWN.put(Material.GILDED_BLACKSTONE, 400L);

        // 1.17
        if (Enums.getIfPresent(Material.class, "DEEPSLATE").isPresent()) {
            COOLDOWN.put(Material.DEEPSLATE_COAL_ORE, 100L);
            COOLDOWN.put(Material.DEEPSLATE_DIAMOND_ORE, 600L);
            COOLDOWN.put(Material.DEEPSLATE_EMERALD_ORE, 600L);
            COOLDOWN.put(Material.DEEPSLATE_GOLD_ORE, 400L);
            COOLDOWN.put(Material.DEEPSLATE_IRON_ORE, 200L);
            COOLDOWN.put(Material.DEEPSLATE_LAPIS_ORE, 200L);
            COOLDOWN.put(Material.DEEPSLATE_REDSTONE_ORE, 200L);
            COOLDOWN.put(Material.COPPER_ORE, 100L);
            COOLDOWN.put(Material.DEEPSLATE_COPPER_ORE, 100L);
        }
        // 1.19
        if (Enums.getIfPresent(Material.class, "MANGROVE_LOG").isPresent()) {
            WOODS.add(Material.MANGROVE_LOG);
            WOODS.add(Material.STRIPPED_MANGROVE_LOG);
        }
        // 1.20
        if (Enums.getIfPresent(Material.class, "CHERRY_LOG").isPresent()) {
            WOODS.add(Material.CHERRY_LOG);
            WOODS.add(Material.STRIPPED_CHERRY_LOG);
        }

        NO_BLOCK_COOLDOWN.put(Material.MELON, 100L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Consumer<Block> cb = QUEUED_ON_DAMAGE.get(e.getPlayer().getUniqueId());
        if (cb != null) {
            cb.accept(e.getBlock());
            QUEUED_ON_DAMAGE.remove(e.getPlayer().getUniqueId());
            e.setCancelled(true);
            return;
        }

        ANNIPlugin plugin = ANNIPlugin.getInstance();
        ANNIArena current = plugin.getCurrentGame();

        if (current.getCopyWorld() != null && current.getMap() != null) {
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
                    } else if (current.getState().canDestroyNexus()) { // 現在のフェーズで破壊できるなら
                        current.damageNexusHealth(ent.getKey(), current.getState().getNexusDamage(), e.getPlayer());
                        if (current.isNexusLost(ent.getKey())) {
                            Nexus.finalDestroyEffects(loc);
                            Bukkit.getScheduler().runTask(plugin, () -> e.getBlock().setType(Material.BEDROCK));
                        } else {
                            Nexus.destroyEffects(loc);
                            Bukkit.getScheduler().runTaskLater(plugin, () -> e.getBlock().setType(Material.END_STONE), 3);
                        }
                    } else { // 現在のフェーズで破壊できないなら
                        e.getPlayer().sendMessage(plugin.getMessageManager().build("nexus.now_cant_destroy"));
                        e.setCancelled(true);
                    }

                    return;
                }
            }
        }

        if (current.getState().getId() >= 0) {
            if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                ItemStack mainHand = e.getPlayer().getInventory().getItemInMainHand();

                if (COOLDOWN.containsKey(e.getBlock().getType())) {
                    if (e.getBlock().getDrops(mainHand).isEmpty()) {
                        e.setCancelled(true);
                        return;
                    }
                    if (((e.getBlock().getType() == Material.DIAMOND_ORE || e.getBlock().getType() == Material.EMERALD_ORE)) && current.getState().getId() < 3) {
                        e.getPlayer().sendMessage(ANNIPlugin.getInstance().getMessageManager().build("notify.cant_mine_now", new ItemStack(e.getBlock().getType()).getItemMeta().getLocalizedName()));
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
                        }, COOLDOWN.get(typ) - 1);
                    });
                }
                else if (NO_BLOCK_COOLDOWN.containsKey(e.getBlock().getType())) {
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
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        e.getBlock().setType(typ); // クールダウン終了後再生成
                        e.getBlock().setBlockData(cloned); // 複製したデータに変更
                    }, NO_BLOCK_COOLDOWN.get(typ));
                }
                else if (WOODS.contains(e.getBlock().getType())) { // 木の処理
                    RegionContainer rc = WorldGuard.getInstance().getPlatform().getRegionContainer();

                    for (
                            ProtectedRegion pr : rc.get(BukkitAdapter.adapt(current.getCopyWorld())).getRegions().values()
                    ) {
                        if (pr.getId().startsWith("anni-wood") && pr.contains(BukkitAdapter.asBlockVector(e.getBlock().getLocation()))) {
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
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                e.getBlock().setType(typ); // クールダウン終了後再生成
                                e.getBlock().setBlockData(cloned); // 複製したデータに変更
                            }, 100);
                            break;
                        }
                    }
                }
                else if (e.getBlock().getType() == Material.WHEAT) {
                    e.setDropItems(false);
                    Ageable ageab = ((Ageable) e.getBlock().getBlockData());

                    if (ageab.getAge() >= ageab.getMaximumAge()) {
                        if (new Random().nextDouble() < 0.01) { // 1%
                            CmnUtil.giveOrDrop(e.getPlayer(), ItemStackBuilder.of(Material.APPLE).build());
                        }
                    }

                    ItemStack[] arr = e.getBlock().getDrops(e.getPlayer().getInventory().getItemInMainHand()).stream()
                            .filter(is -> is.getType() != Material.WHEAT_SEEDS)
                            .toArray(ItemStack[]::new);

                    Location loc = e.getBlock().getLocation().clone();
                    Bukkit.getScheduler().runTaskLater(plugin, () ->
                        loc.getBlock().setType(Material.WHEAT)
                    , 20);

                    CmnUtil.giveOrDrop(e.getPlayer(), arr);
                }
            }
        }
    }

}
