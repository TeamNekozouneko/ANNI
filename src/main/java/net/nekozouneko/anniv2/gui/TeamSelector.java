package net.nekozouneko.anniv2.gui;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.arena.team.ANNITeam;
import net.nekozouneko.anniv2.message.MessageManager;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
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

    private final MessageManager mm = plugin.getMessageManager();

    public TeamSelector(ANNIPlugin plugin, Player player, Set<ANNITeam> disabled, boolean disableRandom, Consumer<ANNITeam> onSelect) {
        super(plugin, player);

        this.onSelect = onSelect;
        this.disabled = disabled;
        this.disableRandom = disableRandom;
    }

    @Override
    public void update() {
        if (inventory == null)
            inventory = Bukkit.createInventory(this, 9, mm.build("gui.team_selector.title"));

        for (int i = 0; i < inventory.getSize(); i++)
            inventory.setItem(i, ItemStackBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build()
            );

        final NamespacedKey tea = new NamespacedKey(plugin, "select-team");

        if (!disabled.contains(ANNITeam.RED)) {
            inventory.setItem(0,
                    ItemStackBuilder.of(Material.RED_WOOL)
                            .name(ANNITeam.RED.getColoredName())
                            .persistentData(tea, PersistentDataType.STRING, ANNITeam.RED.name())
                            .build()
            );
        }

        if (!disabled.contains(ANNITeam.BLUE)) {
            inventory.setItem(1,
                    ItemStackBuilder.of(Material.BLUE_WOOL)
                            .name(ANNITeam.BLUE.getColoredName())
                            .persistentData(tea, PersistentDataType.STRING, ANNITeam.BLUE.name())
                            .build()
            );
        }

        if (!disabled.contains(ANNITeam.GREEN)) {
            inventory.setItem(2,
                    ItemStackBuilder.of(Material.GREEN_WOOL)
                            .name(ANNITeam.GREEN.getColoredName())
                            .persistentData(tea, PersistentDataType.STRING, ANNITeam.GREEN.name())
                            .build()
            );
        }

        if (!disabled.contains(ANNITeam.YELLOW)) {
            inventory.setItem(1,
                    ItemStackBuilder.of(Material.YELLOW_WOOL)
                            .name(ANNITeam.YELLOW.getColoredName())
                            .persistentData(tea, PersistentDataType.STRING, ANNITeam.YELLOW.name())
                            .build()
            );
        }

        if (!disableRandom) {
            inventory.setItem(8,
                    ItemStackBuilder.of(Material.WHITE_WOOL)
                            .name(mm.build("gui.team_selector.random"))
                            .persistentData(tea, PersistentDataType.STRING, "@random")
                            .build()
            );
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
            if (res.equals("@random")) {
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
