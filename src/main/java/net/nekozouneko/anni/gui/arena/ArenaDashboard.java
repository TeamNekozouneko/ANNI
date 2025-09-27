package net.nekozouneko.anni.gui.arena;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.ANNIArena;
import net.nekozouneko.anni.arena.ArenaState;
import net.nekozouneko.anni.arena.team.ANNITeam;
import net.nekozouneko.anni.gui.AbstractGui;
import net.nekozouneko.anni.gui.map.MapSelector;
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

public class ArenaDashboard extends AbstractGui {

    private final TranslationManager tm = plugin.getTranslationManager();
    private final ANNIArena arena = plugin.getCurrentGame();
    private final NamespacedKey act = new NamespacedKey(plugin, "arena-action");

    public ArenaDashboard(ANNIPlugin plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void update() {
        if (inventory == null)
            inventory = Bukkit.createInventory(this, 27, tm.component(player, "gui.arena_dashboard.title"));

        inventory.clear();
        for (int i = 0; i < inventory.getSize(); i++)
            inventory.setItem(i,
                    ItemStackBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
                            .name(" ")
                            .build()
            );

        var red = ItemStack.of(Material.RED_WOOL);
        red.editMeta(m -> m.displayName(tm.component(player, ANNITeam.RED.getNameKey())));

        inventory.setItem(1, red);

        var blue = ItemStack.of(Material.BLUE_WOOL);
        blue.editMeta(m -> m.displayName(tm.component(player, ANNITeam.BLUE.getNameKey())));

        inventory.setItem(2, blue);

        var green = ItemStack.of(Material.GREEN_WOOL);
        green.editMeta(m -> m.displayName(tm.component(player, ANNITeam.GREEN.getNameKey())));

        inventory.setItem(3, green);

        var yellow = ItemStack.of(Material.YELLOW_WOOL);
        yellow.editMeta(m -> m.displayName(tm.component(player, ANNITeam.YELLOW.getNameKey())));

        inventory.setItem(4, yellow);

        var toggleRed = ItemStack.of(arena.isEnabledTeam(ANNITeam.RED) ? Material.END_STONE : Material.BEDROCK);
        toggleRed.editMeta(m -> m.displayName(
                arena.isEnabledTeam(ANNITeam.RED) ?
                        tm.component(player, "gui.arena_dashboard.enabled_nexus") :
                        tm.component(player, "gui.arena_dashboard.disabled_nexus")
                )
        );
        CmnUtil.editPDC(toggleRed, c -> c.set(act, PersistentDataType.STRING, "toggle-red"));
        inventory.setItem(10, toggleRed);

        var toggleBlue = ItemStack.of(arena.isEnabledTeam(ANNITeam.BLUE) ? Material.END_STONE : Material.BEDROCK);
        toggleBlue.editMeta(m -> m.displayName(
                        arena.isEnabledTeam(ANNITeam.BLUE) ?
                                tm.component(player, "gui.arena_dashboard.enabled_nexus") :
                                tm.component(player, "gui.arena_dashboard.disabled_nexus")
                )
        );
        CmnUtil.editPDC(toggleBlue, c -> c.set(act, PersistentDataType.STRING, "toggle-blue"));
        inventory.setItem(11, toggleBlue);

        var toggleGreen = ItemStack.of(arena.isEnabledTeam(ANNITeam.GREEN) ? Material.END_STONE : Material.BEDROCK);
        toggleGreen.editMeta(m -> m.displayName(
                        arena.isEnabledTeam(ANNITeam.GREEN) ?
                                tm.component(player, "gui.arena_dashboard.enabled_nexus") :
                                tm.component(player, "gui.arena_dashboard.disabled_nexus")
                )
        );
        CmnUtil.editPDC(toggleGreen, c -> c.set(act, PersistentDataType.STRING, "toggle-green"));
        inventory.setItem(12, toggleGreen);

        var toggleYellow = ItemStack.of(arena.isEnabledTeam(ANNITeam.YELLOW) ? Material.END_STONE : Material.BEDROCK);
        toggleYellow.editMeta(m -> m.displayName(
                        arena.isEnabledTeam(ANNITeam.YELLOW) ?
                                tm.component(player, "gui.arena_dashboard.enabled_nexus") :
                                tm.component(player, "gui.arena_dashboard.disabled_nexus")
                )
        );
        CmnUtil.editPDC(toggleYellow, c -> c.set(act, PersistentDataType.STRING, "toggle-yellow"));
        inventory.setItem(13, toggleYellow);

        // マップ
        var map = ItemStack.of(Material.MAP);
        map.editMeta(m -> m.displayName(tm.component("gui.arena_dashboard.select_map")));
        CmnUtil.editPDC(map, c -> c.set(act, PersistentDataType.STRING, "select-map"));

        inventory.setItem(15, map);

        Material icon;
        String displayNameKey;
        switch (arena.getState()) {
            case STOPPED: {
                icon = Material.YELLOW_CONCRETE;
                displayNameKey = "gui.arena_dashboard.launch.restore";
                break;
            }
            case WAITING:
            case STARTING: {
                icon = Material.LIME_CONCRETE;
                displayNameKey = "gui.arena_dashboard.launch.start";
                break;
            }
            default: {
                icon = Material.REDSTONE_BLOCK;
                displayNameKey = "gui.arena_dashboard.launch.end_now";
                break;
            }
        }

        ItemStack launchButton = ItemStack.of(icon);
        launchButton.editMeta(m -> m.displayName(tm.component(player, displayNameKey)));
        CmnUtil.editPDC(launchButton, c -> c.set(act, PersistentDataType.STRING, "launch"));

        inventory.setItem(26, launchButton);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() != this) return;

        update();
        e.setCancelled(true);

        if (e.getCurrentItem() == null || e.getCurrentItem().getType().isAir()) return;

        ItemStack item = e.getCurrentItem();
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();

        switch (pdc.getOrDefault(act, PersistentDataType.STRING, "")) {
            case "toggle-red": {
                toggleTeam(ANNITeam.RED);
                break;
            }
            case "toggle-blue": {
                toggleTeam(ANNITeam.BLUE);
                break;
            }
            case "toggle-green": {
                toggleTeam(ANNITeam.GREEN);
                break;
            }
            case "toggle-yellow": {
                toggleTeam(ANNITeam.YELLOW);
                break;
            }
            case "select-map": {
                player.closeInventory();
                Bukkit.getScheduler().runTask(plugin, () ->
                    new MapSelector(plugin, player, 1, true, (map) -> {
                        arena.setMap(map);
                        if (map != null)
                            player.sendMessage(tm.component(player, "command.arena.set_map", map.getId()));
                        else player.sendMessage(tm.component(player,"command.arena.map_random"));
                    }).open()
                );
                break;
            }
            case "launch": {
                switch (arena.getState()) {
                    case STOPPED:
                        arena.setState(ArenaState.WAITING);
                        break;
                    case WAITING:
                    case STARTING:
                        player.sendMessage(tm.component(player, "command.arena.starting"));
                        if (!arena.start()) {
                            player.sendMessage(tm.component(player, "command.error.game_start_failed"));
                        }
                        break;
                    default:
                        arena.setState(ArenaState.GAME_OVER);
                        arena.setTimer(1);
                        break;
                }
                break;
            }
        }

        update();
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() != this) return;

        unregisterAllGuiListeners(((Player) e.getPlayer()));
    }

    private void toggleTeam(ANNITeam at) {
        if (arena.isEnabledTeam(at)) {
            if (arena.getTeams().size() > 2) {
                arena.disableTeam(at);
            }
            else {
                player.sendMessage(tm.component(player, "command.error.disable_team_limited"));
                return;
            }
        }
        else arena.enableTeam(at);

        if (arena.isEnabledTeam(at))
            player.sendMessage(tm.component(player,"command.arena.enabled_team", tm.component(player, at.getNameKey())));
        else player.sendMessage(tm.component(player, "command.arena.disabled_team", tm.component(player, at.getNameKey())));
    }
}
