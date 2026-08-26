package net.nekozouneko.anni.task;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.kit.ANNIKit;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Function;

public class RechargeManager extends BukkitRunnable {

    @AllArgsConstructor @Getter
    public enum Type {
        STUN_GRENADE(5, player ->
                ANNIKit.MOCHI_MOCHI.getKit().equals(ANNIPlugin.getInstance().getCurrentGame().getKit(player)),
                "stun-grenade",
                30000
        );

        private final int limit;
        private final Function<Player, Boolean> condition;
        private final String specialKey;
        private final long cooldown;
    }

    private final Table<Player, Type, Long> recharges = HashBasedTable.create();

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            for (Type type : Type.values()) {
                if (recharges.get(player, type) != null && (!type.getCondition().apply(player) || type.getLimit() <= countItem(player, type)-1) || countItem(player, type) == 0) {
                    recharges.remove(player, type);
                    continue;
                }

                Long rechargeTime = recharges.get(player, type);

                if (rechargeTime != null && rechargeTime <= System.currentTimeMillis()) {
                    recharges.remove(player, type);
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item == null || item.getType().isAir()) continue;

                        String specialItemType = item.getPersistentDataContainer().get(new NamespacedKey(ANNIPlugin.getInstance(), "special-item"), PersistentDataType.STRING);

                        if (type.getSpecialKey().equals(specialItemType)) {
                            item.add();
                            break;
                        }
                    }
                }

                if (recharges.contains(player, type)) continue;

                long count = countItem(player, type);
                if (type.getLimit() <= count-1) continue;

                recharges.put(player, type, System.currentTimeMillis()+type.getCooldown());
            }
        });
    }

    public void clear() {
        recharges.clear();
    }

    public void clear(Player player) {
        for (Type type : Type.values())
            recharges.remove(player, type);
    }

    private long countItem(Player player, Type type) {
        int count = 0;
        for (ItemStack item : player.getInventory()) {
            if (item == null || item.getType().isAir()) continue;

            String specialItemType = item.getPersistentDataContainer().get(new NamespacedKey(ANNIPlugin.getInstance(), "special-item"), PersistentDataType.STRING);
            if (!type.getSpecialKey().equals(specialItemType)) continue;

            count += item.getAmount();
        }

        return count;
    }

    @Override
    public void cancel() {
        super.cancel();
        clear();
    }

    public String getTimeLeftFormatted(Player player, Type type) {
        Long l = recharges.get(player, type);
        if (l == null) l = 0L;
        double left = Math.max(0, ((double) l / 1000) - ((double) System.currentTimeMillis() / 1000));

        return String.format("%.1f", left);
    }
}
