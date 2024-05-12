package net.nekozouneko.anni.listener;

import com.google.common.base.Enums;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.ANNIArena;
import net.nekozouneko.anni.arena.team.ANNITeam;
import net.nekozouneko.anni.map.Nexus;
import net.nekozouneko.anni.message.MessageManager;
import net.nekozouneko.anni.util.CmnUtil;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class BlockBreakListener implements Listener {

    @AllArgsConstructor
    public static class ANNIBlockInfo {
        @Getter
        private final int cooldown;
        @Getter
        private final boolean rare;
        @Getter
        private final Material blockOfInCooldown;
        private final IntSupplier xp;

        public int getXp() {
            return xp.getAsInt();
        }
    }

    private static class RegenerateBlockTask extends BukkitRunnable {
        private int count = 0;
        private final ANNIBlockInfo info;
        private final Material block;
        private final BlockData data;
        private final Location location;

        private RegenerateBlockTask(ANNIBlockInfo info, Material block, BlockData data, Location location) {
            this.info = info;
            this.block = block;
            this.data = data.clone();
            this.location = location.clone();
        }

        @Override
        public void run() {
            if (!location.isWorldLoaded()) {
                cancel();
                return;
            }

            if (count == 0) {
                if (!(info.getBlockOfInCooldown() == null || info.getBlockOfInCooldown().isAir()))
                    location.getBlock().setType(info.getBlockOfInCooldown());
                count++;
                return;
            }

            if (count >= info.getCooldown()) {
                location.getBlock().setType(block);
                location.getBlock().setBlockData(data);
                cancel();
                return;
            }

            count++;
        }
    }

    private static final Map<UUID, Consumer<Block>> QUEUED_ON_DAMAGE = new HashMap<>();
    private static final List<Material> WOODS = new ArrayList<>(Arrays.asList(
            Material.ACACIA_LOG, Material.BIRCH_LOG, Material.DARK_OAK_LOG,
            Material.JUNGLE_LOG, Material.OAK_LOG, Material.SPRUCE_LOG,
            Material.CRIMSON_STEM, Material.WARPED_STEM,
            Material.STRIPPED_ACACIA_LOG, Material.STRIPPED_BIRCH_LOG,
            Material.STRIPPED_DARK_OAK_LOG, Material.STRIPPED_JUNGLE_LOG,
            Material.STRIPPED_OAK_LOG, Material.STRIPPED_SPRUCE_LOG,
            Material.STRIPPED_CRIMSON_STEM, Material.STRIPPED_WARPED_STEM
    ));
    private static final Map<Material, ANNIBlockInfo> BLOCKS = new EnumMap<>(Material.class);

    public static Map<UUID, Consumer<Block>> getQueuedOnDamageMap() {
        return QUEUED_ON_DAMAGE;
    }

    public static Set<Material> getRegenerativeBlocks() {
        return Collections.unmodifiableSet(BLOCKS.keySet());
    }

    static {
        BLOCKS.put(Material.MELON, new ANNIBlockInfo(5, false, null, () -> 0));
        BLOCKS.put(Material.GRAVEL, new ANNIBlockInfo(5, false, Material.COBBLESTONE, () -> 0));

        ANNIBlockInfo lowInfo = new ANNIBlockInfo(
                5, false, Material.COBBLESTONE,
                () -> new Random().nextInt(4)
        );
        ANNIBlockInfo commonInfo = new ANNIBlockInfo(
                10, false, Material.COBBLESTONE,
                () -> new Random().nextInt(3) + 3
        );
        ANNIBlockInfo rareInfo = new ANNIBlockInfo(
                30, true, Material.COBBLESTONE,
                () -> new Random().nextInt(8) + 7
        );

        BLOCKS.put(Material.COAL_ORE, lowInfo);
        BLOCKS.put(Material.IRON_ORE, commonInfo);
        BLOCKS.put(Material.NETHER_QUARTZ_ORE, commonInfo);
        BLOCKS.put(Material.REDSTONE_ORE, commonInfo);
        BLOCKS.put(Material.LAPIS_ORE, commonInfo);
        BLOCKS.put(Material.GOLD_ORE, new ANNIBlockInfo(
                20, false, Material.COBBLESTONE,
                () -> new Random().nextInt(7) + 6
        ));
        BLOCKS.put(Material.DIAMOND_ORE, rareInfo);
        BLOCKS.put(Material.EMERALD_ORE, new ANNIBlockInfo(30, true, Material.COBBLESTONE, () -> new Random().nextInt(15) + 10));
        BLOCKS.put(Material.NETHER_GOLD_ORE, commonInfo);

        // 1.17
        if (Enums.getIfPresent(Material.class, "DEEPSLATE").isPresent()) {
            BLOCKS.put(Material.DEEPSLATE_COAL_ORE, lowInfo);
            BLOCKS.put(Material.COPPER_ORE, lowInfo);
            BLOCKS.put(Material.DEEPSLATE_COPPER_ORE, lowInfo);
            BLOCKS.put(Material.DEEPSLATE_IRON_ORE, commonInfo);
            BLOCKS.put(Material.DEEPSLATE_LAPIS_ORE, commonInfo);
            BLOCKS.put(Material.DEEPSLATE_REDSTONE_ORE, commonInfo);
            BLOCKS.put(Material.DEEPSLATE_GOLD_ORE, new ANNIBlockInfo(
                    20, false, Material.COBBLESTONE,
                    () -> new Random().nextInt(7) + 6
            ));
            BLOCKS.put(Material.DEEPSLATE_DIAMOND_ORE, rareInfo);
            BLOCKS.put(Material.DEEPSLATE_EMERALD_ORE, new ANNIBlockInfo(30, true, Material.COBBLESTONE, () -> new Random().nextInt(15) + 10));
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
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        MessageManager mm = ANNIPlugin.getInstance().getMessageManager();

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

                if (e.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    e.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
                    e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 2);
                    e.getPlayer().sendMessage(mm.build("notify.removed_invisibility"));
                }

                if (BLOCKS.containsKey(e.getBlock().getType())) {
                    if (e.getBlock().getDrops(mainHand).isEmpty()) {
                        e.setCancelled(true);
                        return;
                    }

                    ANNIBlockInfo info = BLOCKS.get(e.getBlock().getType());
                    if (info == null) return;

                    if (info.isRare() && current.getState().getId() < 3) {
                        e.getPlayer().sendMessage(mm.build("notify.cant_mine_now"));
                        e.setCancelled(true);
                        return;
                    }

                    if (e.getBlock().getType() != Material.EMERALD_ORE) {
                        CmnUtil.giveOrDrop(e.getPlayer(), e.getBlock().getDrops(mainHand, e.getPlayer()));
                    }

                    e.setExpToDrop(0);
                    e.setDropItems(false);
                    int exp = info.getXp();
                    if (exp > 0) e.getPlayer().giveExp(exp);

                    new RegenerateBlockTask(info, e.getBlock().getType(), e.getBlock().getBlockData(), e.getBlock().getLocation())
                            .runTaskTimer(ANNIPlugin.getInstance(), 0, 20);
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
