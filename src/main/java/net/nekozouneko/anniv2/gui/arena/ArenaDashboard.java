package net.nekozouneko.anniv2.gui.arena;

import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.arena.ANNIArena;
import net.nekozouneko.anniv2.arena.ArenaState;
import net.nekozouneko.anniv2.arena.team.ANNITeam;
import net.nekozouneko.anniv2.gui.AbstractGui;
import net.nekozouneko.anniv2.gui.map.MapSelector;
import net.nekozouneko.anniv2.message.MessageManager;
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

//TODO いい加減作れよ
public class ArenaDashboard extends AbstractGui {

    private final MessageManager mm = plugin.getMessageManager();
    private final ANNIArena arena = plugin.getCurrentGame();
    private final NamespacedKey act = new NamespacedKey(plugin, "arena-action");

    public ArenaDashboard(ANNIPlugin plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void update() {
        if (inventory == null)
            inventory = Bukkit.createInventory(this, 27, mm.build("gui.arena_dashboard.title", arena.getId()));

        inventory.clear();
        for (int i = 0; i < inventory.getSize(); i++)
            inventory.setItem(i,
                    ItemStackBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
                            .name(" ")
                            .build()
            );

        inventory.setItem(1,
                ItemStackBuilder.of(Material.RED_WOOL)
                        .name(ANNITeam.RED.getColoredName())
                        .build()
        );
        inventory.setItem(2,
                ItemStackBuilder.of(Material.BLUE_WOOL)
                        .name(ANNITeam.BLUE.getColoredName())
                        .build()
        );
        inventory.setItem(3,
                ItemStackBuilder.of(Material.GREEN_WOOL)
                        .name(ANNITeam.GREEN.getColoredName())
                        .build()
        );
        inventory.setItem(4,
                ItemStackBuilder.of(Material.YELLOW_WOOL)
                        .name(ANNITeam.YELLOW.getColoredName())
                        .build()
        );

        inventory.setItem(10,
                ItemStackBuilder.of(arena.isEnabledTeam(ANNITeam.RED) ? Material.END_STONE : Material.BEDROCK)
                        .name(arena.isEnabledTeam(ANNITeam.RED) ?
                                mm.build("gui.arena_dashboard.enabled_nexus") :
                                mm.build("gui.arena_dashboard.disabled_nexus"))
                        .persistentData(act, PersistentDataType.STRING, "toggle-red")
                        .build()
        );
        inventory.setItem(11,
                ItemStackBuilder.of(arena.isEnabledTeam(ANNITeam.BLUE) ? Material.END_STONE : Material.BEDROCK)
                        .name(arena.isEnabledTeam(ANNITeam.BLUE) ?
                                mm.build("gui.arena_dashboard.enabled_nexus") :
                                mm.build("gui.arena_dashboard.disabled_nexus"))
                        .persistentData(act, PersistentDataType.STRING, "toggle-blue")
                        .build()
        );
        inventory.setItem(12,
                ItemStackBuilder.of(arena.isEnabledTeam(ANNITeam.GREEN) ? Material.END_STONE : Material.BEDROCK)
                        .name(arena.isEnabledTeam(ANNITeam.GREEN) ?
                                mm.build("gui.arena_dashboard.enabled_nexus") :
                                mm.build("gui.arena_dashboard.disabled_nexus"))
                        .persistentData(act, PersistentDataType.STRING, "toggle-green")
                        .build()
        );
        inventory.setItem(13,
                ItemStackBuilder.of(arena.isEnabledTeam(ANNITeam.YELLOW) ? Material.END_STONE : Material.BEDROCK)
                        .name(arena.isEnabledTeam(ANNITeam.YELLOW) ?
                                mm.build("gui.arena_dashboard.enabled_nexus") :
                                mm.build("gui.arena_dashboard.disabled_nexus"))
                        .persistentData(act, PersistentDataType.STRING, "toggle-yellow")
                        .build()
        );

        // マップ
        inventory.setItem(6,
                ItemStackBuilder.of(Material.MAP)
                        .name(mm.build("gui.arena_dashboard.select_map"))
                        .persistentData(act, PersistentDataType.STRING, "select-map")
                        .build()
        );

        Material icon;
        String display;
        switch (arena.getState()) {
            case STOPPED: {
                icon = Material.YELLOW_CONCRETE;
                display = mm.build("gui.arena_dashboard.launch.restore");
                break;
            }
            case WAITING:
            case STARTING: {
                icon = Material.LIME_CONCRETE;
                display = mm.build("gui.arena_dashboard.launch.start");
                break;
            }
            default: {
                icon = Material.REDSTONE_BLOCK;
                display = mm.build("gui.arena_dashboard.launch.end_now");
                break;
            }
        }

        inventory.setItem(26,
                ItemStackBuilder.of(icon)
                        .name(display)
                        .persistentData(act, PersistentDataType.STRING, "launch")
                        .build()
        );
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
                            player.sendMessage(mm.build("command.arena.set_map", map.getId()));
                        else player.sendMessage(mm.build("command.arena.map_random"));
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
                        player.sendMessage(mm.build("command.arena.starting"));
                        if (!arena.start()) {
                            player.sendMessage(mm.build("command.err.game_start_failed"));
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
                player.sendMessage(mm.build("command.err.no_more_teams_can_be_disabled"));
                return;
            }
        }
        else arena.enableTeam(at);

        if (arena.isEnabledTeam(at))
            player.sendMessage(mm.build("command.arena.enabled_team", at.getTeamName()));
        else player.sendMessage(mm.build("command.arena.disabled_team", at.getTeamName()));
    }
}
