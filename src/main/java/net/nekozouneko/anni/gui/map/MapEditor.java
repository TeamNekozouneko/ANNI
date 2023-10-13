package net.nekozouneko.anni.gui.map;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.team.ANNITeam;
import net.nekozouneko.anni.gui.AbstractGui;
import net.nekozouneko.anni.listener.BlockBreakListener;
import net.nekozouneko.anni.map.ANNIMap;
import net.nekozouneko.anni.map.SpawnLocation;
import net.nekozouneko.anni.message.MessageManager;
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

public class MapEditor extends AbstractGui {

    private final ANNIMap map;

    public MapEditor(ANNIPlugin plugin, Player player, ANNIMap map) {
        super(plugin, player);

        this.map = map;
    }

    @Override
    public void update() {
        final MessageManager mm = plugin.getMessageManager();
        NamespacedKey act = new NamespacedKey(plugin, "action");

        if (inventory == null)
            inventory = Bukkit.createInventory(
                    this, 27,
                    mm.build("gui.map_editor.title", map.getId())
            );
        inventory.clear();

        for (int i = 0; i < inventory.getSize(); i++)
            inventory.setItem(i,
                    ItemStackBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
                            .name(" ")
                            .build()
            );

        // デフォルトスポーン地点の設定
        inventory.setItem(0,
                ItemStackBuilder.of(Material.WHITE_BED)
                        .name(mm.build("gui.map_editor.default_spawn"))
                        .persistentData(act, PersistentDataType.STRING, "default_spawn")
                        .build()
        );

        // 各チーム
        inventory.setItem(2,
                ItemStackBuilder.of(Material.RED_WOOL)
                        .name(ANNITeam.RED.getColoredName())
                        .build()
        );
        inventory.setItem(3,
                ItemStackBuilder.of(Material.BLUE_WOOL)
                        .name(ANNITeam.BLUE.getColoredName())
                        .build()
        );
        inventory.setItem(4,
                ItemStackBuilder.of(Material.GREEN_WOOL)
                        .name(ANNITeam.GREEN.getColoredName())
                        .build()
        );
        inventory.setItem(5,
                ItemStackBuilder.of(Material.YELLOW_WOOL)
                        .name(ANNITeam.YELLOW.getColoredName())
                        .build()
        );

        // 各チームのスポーン地点
        inventory.setItem(11,
                ItemStackBuilder.of(Material.RED_BED)
                        .name(mm.build(
                                "gui.map_editor.team_spawn",
                                ANNITeam.RED.getColoredName()
                        ))
                        .persistentData(act, PersistentDataType.STRING, "red_spawn")
                        .build()
        );
        inventory.setItem(12,
                ItemStackBuilder.of(Material.BLUE_BED)
                        .name(mm.build(
                                "gui.map_editor.team_spawn",
                                ANNITeam.BLUE.getColoredName()
                        ))
                        .persistentData(act, PersistentDataType.STRING, "blue_spawn")
                        .build()
        );
        inventory.setItem(13,
                ItemStackBuilder.of(Material.GREEN_BED)
                        .name(mm.build(
                                "gui.map_editor.team_spawn",
                                ANNITeam.GREEN.getColoredName()
                        ))
                        .persistentData(act, PersistentDataType.STRING, "green_spawn")
                        .build()
        );
        inventory.setItem(14,
                ItemStackBuilder.of(Material.YELLOW_BED)
                        .name(mm.build(
                                "gui.map_editor.team_spawn",
                                ANNITeam.YELLOW.getColoredName()
                        ))
                        .persistentData(act, PersistentDataType.STRING, "yellow_spawn")
                        .build()
        );

        // 各チームのネクサス
        inventory.setItem(20,
                ItemStackBuilder.of(Material.END_STONE)
                        .name(mm.build(
                                "gui.map_editor.team_nexus",
                                ANNITeam.RED.getColoredName()
                        ))
                        .persistentData(act, PersistentDataType.STRING, "red_nexus")
                        .build()
        );
        inventory.setItem(21,
                ItemStackBuilder.of(Material.END_STONE)
                        .name(mm.build(
                                "gui.map_editor.team_nexus",
                                ANNITeam.BLUE.getColoredName()
                        ))
                        .persistentData(act, PersistentDataType.STRING, "blue_nexus")
                        .build()
        );
        inventory.setItem(22,
                ItemStackBuilder.of(Material.END_STONE)
                        .name(mm.build(
                                "gui.map_editor.team_nexus",
                                ANNITeam.GREEN.getColoredName()
                        ))
                        .persistentData(act, PersistentDataType.STRING, "green_nexus")
                        .build()
        );
        inventory.setItem(23,
                ItemStackBuilder.of(Material.END_STONE)
                        .name(mm.build(
                                "gui.map_editor.team_nexus",
                                ANNITeam.YELLOW.getColoredName()
                        ))
                        .persistentData(act, PersistentDataType.STRING, "yellow_nexus")
                        .build()
        );

        inventory.setItem(18,
                ItemStackBuilder.of(Material.ENDER_PEARL)
                    .name(mm.build("gui.map_editor.tp_to_world"))
                    .persistentData(act,
                            PersistentDataType.STRING, "teleport"
                    )
                    .build()
        );
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() != this) return;

        ItemStack item = e.getCurrentItem();
        MessageManager mm = plugin.getMessageManager();

        if (item == null) return;

        PersistentDataContainer c = item.getItemMeta().getPersistentDataContainer();

        e.setCancelled(true);

        if (c.has(new NamespacedKey(plugin, "action"), PersistentDataType.STRING)) {
            switch (c.getOrDefault(new NamespacedKey(plugin, "action"), PersistentDataType.STRING, "")) {
                case "teleport": {
                    player.closeInventory();
                    player.teleport(map.getBukkitWorld().getSpawnLocation());
                    break;
                }
                case "default_spawn": {
                    map.setDefaultSpawn(player.getLocation().clone());
                    player.sendMessage(mm.build(
                            "gui.map_editor.set_your_location",
                            mm.yawPitchLocationFormat(player.getLocation())
                    ));
                    player.closeInventory();
                    break;
                }
                case "red_spawn": {
                    map.setSpawn(ANNITeam.RED, SpawnLocation.fromLocation(player.getLocation()));
                    player.sendMessage(mm.build(
                            "gui.map_editor.set_your_location_team",
                            ANNITeam.RED.getColoredName(),
                            mm.yawPitchLocationFormat(player.getLocation())
                    ));
                    player.closeInventory();
                    break;
                }
                case "blue_spawn": {
                    map.setSpawn(ANNITeam.BLUE, SpawnLocation.fromLocation(player.getLocation()));
                    player.sendMessage(mm.build(
                            "gui.map_editor.set_your_location_team",
                            ANNITeam.BLUE.getColoredName(),
                            mm.yawPitchLocationFormat(player.getLocation())
                    ));
                    player.closeInventory();
                    break;
                }
                case "green_spawn": {
                    map.setSpawn(ANNITeam.GREEN, SpawnLocation.fromLocation(player.getLocation()));
                    player.sendMessage(mm.build(
                            "gui.map_editor.set_your_location_team",
                            ANNITeam.GREEN.getColoredName(),
                            mm.yawPitchLocationFormat(player.getLocation())
                    ));
                    player.closeInventory();
                    break;
                }
                case "yellow_spawn": {
                    map.setSpawn(ANNITeam.YELLOW, SpawnLocation.fromLocation(player.getLocation()));
                    player.sendMessage(mm.build(
                            "gui.map_editor.set_your_location_team",
                            ANNITeam.YELLOW.getColoredName(),
                            mm.yawPitchLocationFormat(player.getLocation())
                    ));
                    player.closeInventory();
                    break;
                }
                case "red_nexus": {
                    player.closeInventory();
                    player.sendMessage(mm.build("gui.map_editor.please_click_a_block"));

                    BlockBreakListener.getQueuedOnDamageMap().put(player.getUniqueId(), (bl) -> {
                        map.setNexus(ANNITeam.RED, bl.getLocation());
                        player.sendMessage(mm.build(
                                "gui.map_editor.set_team_nexus_loc",
                                ANNITeam.RED.getColoredName(),
                                mm.blockLocationFormat(bl.getLocation())
                        ));
                    });
                    break;
                }
                case "blue_nexus": {
                    player.closeInventory();
                    player.sendMessage(mm.build("gui.map_editor.please_click_a_block"));

                    BlockBreakListener.getQueuedOnDamageMap().put(player.getUniqueId(), (bl) -> {
                        map.setNexus(ANNITeam.BLUE, bl.getLocation());
                        player.sendMessage(mm.build(
                                "gui.map_editor.set_team_nexus_loc",
                                ANNITeam.BLUE.getColoredName(),
                                mm.blockLocationFormat(bl.getLocation())
                        ));
                    });
                    break;
                }
                case "green_nexus": {
                    player.closeInventory();
                    player.sendMessage(mm.build("gui.map_editor.please_click_a_block"));

                    BlockBreakListener.getQueuedOnDamageMap().put(player.getUniqueId(), (bl) -> {
                        map.setNexus(ANNITeam.GREEN, bl.getLocation());
                        player.sendMessage(mm.build(
                                "gui.map_editor.set_team_nexus_loc",
                                ANNITeam.GREEN.getColoredName(),
                                mm.blockLocationFormat(bl.getLocation())
                        ));
                    });
                    break;
                }
                case "yellow_nexus": {
                    player.closeInventory();
                    player.sendMessage(mm.build("gui.map_editor.please_click_a_block"));

                    BlockBreakListener.getQueuedOnDamageMap().put(player.getUniqueId(), (bl) -> {
                        map.setNexus(ANNITeam.YELLOW, bl.getLocation());
                        player.sendMessage(mm.build(
                                "gui.map_editor.set_team_nexus_loc",
                                ANNITeam.YELLOW.getColoredName(),
                                mm.blockLocationFormat(bl.getLocation())
                        ));
                    });
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() != this) return;

        unregisterAllGuiListeners(player);
    }
}
