package net.nekozouneko.anni.gui;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.team.ANNITeam;
import net.nekozouneko.anni.message.TranslationManager;
import net.nekozouneko.anni.util.CmnUtil;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

public class TeamSelector extends AbstractGui {

    private final Consumer<ANNITeam> onSelect;
    private final Set<ANNITeam> disabled;
    private final boolean disableRandom;

    private final TranslationManager tm = plugin.getTranslationManager();

    public TeamSelector(ANNIPlugin plugin, Player player, Set<ANNITeam> disabled, boolean disableRandom, Consumer<ANNITeam> onSelect) {
        super(plugin, player);

        this.onSelect = onSelect;
        this.disabled = disabled;
        this.disableRandom = disableRandom;
    }

    @Override
    public void update() {
        if (inventory == null)
            inventory = Bukkit.createInventory(this, 9, tm.component(player, "gui.team_selector.title"));
        inventory.clear();

        for (int i = 0; i < inventory.getSize(); i++)
            inventory.setItem(i, ItemStackBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build()
            );

        final NamespacedKey tea = new NamespacedKey(plugin, "select-team");

        if (!disabled.contains(ANNITeam.RED)) {
            ItemStack red = ItemStack.of(Material.RED_WOOL);
            red.editMeta(m -> m.displayName(tm.component(player, ANNITeam.RED.getNameKey())));
            CmnUtil.editPDC(red, c -> c.set(tea, PersistentDataType.STRING, ANNITeam.RED.name()));

            inventory.setItem(0, red);
        }

        if (!disabled.contains(ANNITeam.BLUE)) {
            ItemStack blue = ItemStack.of(Material.BLUE_WOOL);
            blue.editMeta(m -> m.displayName(tm.component(player, ANNITeam.BLUE.getNameKey())));
            CmnUtil.editPDC(blue, c -> c.set(tea, PersistentDataType.STRING, ANNITeam.BLUE.name()));

            inventory.setItem(1, blue);
        }

        if (!disabled.contains(ANNITeam.GREEN)) {
            ItemStack green = ItemStack.of(Material.GREEN_WOOL);
            green.editMeta(m -> m.displayName(tm.component(player, ANNITeam.GREEN.getNameKey())));
            CmnUtil.editPDC(green, c -> c.set(tea, PersistentDataType.STRING, ANNITeam.GREEN.name()));

            inventory.setItem(2, green);
        }

        if (!disabled.contains(ANNITeam.YELLOW)) {
            ItemStack yellow = ItemStack.of(Material.YELLOW_WOOL);
            yellow.editMeta(m -> m.displayName(tm.component(player, ANNITeam.YELLOW.getNameKey())));
            CmnUtil.editPDC(yellow, c -> c.set(tea, PersistentDataType.STRING, ANNITeam.YELLOW.name()));

            inventory.setItem(3, yellow);
        }

        if (!disableRandom) {
            ItemStack random = ItemStack.of(Material.WHITE_WOOL);
            random.editMeta(m -> m.displayName(tm.component(player, "gui.random")));
            CmnUtil.editPDC(random, c -> c.set(tea, PersistentDataType.STRING, "@random"));

            inventory.setItem(8, random);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() != this) return;

        e.setCancelled(true);

        if (e.getCurrentItem() == null || e.getCurrentItem().getType().isAir()) return;

        PersistentDataContainer pdc = e.getCurrentItem().getItemMeta().getPersistentDataContainer();
        final NamespacedKey tea = new NamespacedKey(plugin, "select-team");

        if (pdc.has(tea, PersistentDataType.STRING)) {
            String res = pdc.get(tea, PersistentDataType.STRING);
            if ("@random".equals(res)) {
                ANNITeam[] arr = Arrays.stream(ANNITeam.values())
                        .filter(at -> !disabled.contains(at))
                        .toArray(ANNITeam[]::new);

                player.closeInventory();
                Bukkit.getScheduler().runTask(plugin, () ->
                    onSelect.accept(arr[new Random().nextInt(arr.length)])
                );
            }
            else {
                Optional<ANNITeam> enu = Enums.getIfPresent(ANNITeam.class, res);

                if (enu.isPresent()) {
                    player.closeInventory();
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        onSelect.accept(enu.get());
                    });
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() != this) return;

        AbstractGui.unregisterAllGuiListeners((Player) e.getPlayer());
    }
}
