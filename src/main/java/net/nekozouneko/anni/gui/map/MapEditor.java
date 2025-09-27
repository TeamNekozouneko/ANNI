package net.nekozouneko.anni.gui.map;

import net.kyori.adventure.text.Component;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.team.ANNITeam;
import net.nekozouneko.anni.gui.AbstractGui;
import net.nekozouneko.anni.listener.BlockBreakListener;
import net.nekozouneko.anni.map.ANNIMap;
import net.nekozouneko.anni.map.SpawnLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
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
        var translation = ANNIPlugin.getInstance().getTranslationManager();
        NamespacedKey act = new NamespacedKey(plugin, "action");

        if (inventory == null)
            inventory = Bukkit.createInventory(
                    this, 27,
                    translation.component(player, "gui.map_editor.title", map.getId())
            );
        inventory.clear();

        ItemStack background = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE);
        background.editMeta(meta -> meta.displayName(Component.space()));

        for (int i = 0; i < inventory.getSize(); i++)
            inventory.setItem(i, background);

        // デフォルトスポーン地点の設定
        ItemStack defaultSpawn = ItemStack.of(Material.WHITE_BED);
        defaultSpawn.editMeta(meta -> {
            meta.displayName(translation.component(player, "gui.map_editor.default_spawn"));
            meta.getPersistentDataContainer().set(
                    act, PersistentDataType.STRING, "default_spawn"
            );
        });

        inventory.setItem(0, defaultSpawn);

        // 各チーム
        var red = ItemStack.of(Material.RED_WOOL);
        red.editMeta(m -> m.displayName(translation.component(player, ANNITeam.RED.getNameKey())));

        inventory.setItem(2, red);

        var blue = ItemStack.of(Material.BLUE_WOOL);
        blue.editMeta(m -> m.displayName(translation.component(player, ANNITeam.BLUE.getNameKey())));

        inventory.setItem(3, blue);

        var green = ItemStack.of(Material.GREEN_WOOL);
        green.editMeta(m -> m.displayName(translation.component(player, ANNITeam.GREEN.getNameKey())));

        inventory.setItem(4, green);

        var yellow = ItemStack.of(Material.YELLOW_WOOL);
        yellow.editMeta(m -> m.displayName(translation.component(player, ANNITeam.YELLOW.getNameKey())));

        inventory.setItem(5, yellow);

        // 各チームのスポーン地点
        ItemStack redSpawn = ItemStack.of(Material.RED_BED);
        redSpawn.editMeta(meta -> {
            meta.displayName(translation.component(player,
                    "gui.map_editor.team_spawn",
                    translation.component(player, ANNITeam.RED.getNameKey())
            ));
            meta.getPersistentDataContainer().set(
                    act, PersistentDataType.STRING, "red_spawn"
            );
        });

        ItemStack blueSpawn = ItemStack.of(Material.BLUE_BED);
        blueSpawn.editMeta(meta -> {
            meta.displayName(translation.component(player,
                    "gui.map_editor.team_spawn",
                    translation.component(player, ANNITeam.BLUE.getNameKey())
            ));
            meta.getPersistentDataContainer().set(
                    act, PersistentDataType.STRING, "blue_spawn"
            );
        });

        ItemStack greenSpawn = ItemStack.of(Material.GREEN_BED);
        greenSpawn.editMeta(meta -> {
            meta.displayName(translation.component(player,
                    "gui.map_editor.team_spawn",
                    translation.component(player, ANNITeam.GREEN.getNameKey())
            ));
            meta.getPersistentDataContainer().set(
                    act, PersistentDataType.STRING, "green_spawn"
            );
        });

        ItemStack yellowSpawn = ItemStack.of(Material.YELLOW_BED);
        yellowSpawn.editMeta(meta -> {
            meta.displayName(translation.component(player,
                    "gui.map_editor.team_spawn",
                    translation.component(player, ANNITeam.YELLOW.getNameKey())
            ));
            meta.getPersistentDataContainer().set(
                    act, PersistentDataType.STRING, "yellow_spawn"
            );
        });

        inventory.setItem(11, redSpawn);
        inventory.setItem(12, blueSpawn);
        inventory.setItem(13, greenSpawn);
        inventory.setItem(14, yellowSpawn);

        // 各チームのネクサス
        ItemStack redNexus = ItemStack.of(Material.END_STONE);
        redNexus.editMeta(meta -> {
            meta.displayName(translation.component(player,
                    "gui.map_editor.team_nexus",
                    translation.component(player, ANNITeam.RED.getNameKey())
            ));
            meta.getPersistentDataContainer().set(
                    act, PersistentDataType.STRING, "red_nexus"
            );
        });

        ItemStack blueNexus = ItemStack.of(Material.END_STONE);
        redNexus.editMeta(meta -> {
            meta.displayName(translation.component(player,
                    "gui.map_editor.team_nexus",
                    translation.component(player, ANNITeam.BLUE.getNameKey())
            ));
            meta.getPersistentDataContainer().set(
                    act, PersistentDataType.STRING, "blue_nexus"
            );
        });

        ItemStack greenNexus = ItemStack.of(Material.END_STONE);
        redNexus.editMeta(meta -> {
            meta.displayName(translation.component(player,
                    "gui.map_editor.team_nexus",
                    translation.component(player, ANNITeam.GREEN.getNameKey())
            ));
            meta.getPersistentDataContainer().set(
                    act, PersistentDataType.STRING, "green_nexus"
            );
        });

        ItemStack yellowNexus = ItemStack.of(Material.END_STONE);
        redNexus.editMeta(meta -> {
            meta.displayName(translation.component(player,
                    "gui.map_editor.team_nexus",
                    translation.component(player, ANNITeam.YELLOW.getNameKey())
            ));
            meta.getPersistentDataContainer().set(
                    act, PersistentDataType.STRING, "yellow_nexus"
            );
        });

        inventory.setItem(20, redNexus);
        inventory.setItem(21, blueNexus);
        inventory.setItem(22, greenNexus);
        inventory.setItem(23, yellowNexus);

        ItemStack teleportToTheWorld = ItemStack.of(Material.ENDER_PEARL);
        teleportToTheWorld.editMeta(meta -> {
            meta.displayName(translation.component(player, "gui.map_editor.tp_to_world"));
            meta.getPersistentDataContainer().set(
                    act, PersistentDataType.STRING, "teleport"
            );
        });

        inventory.setItem(18, teleportToTheWorld);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() != this) return;

        ItemStack item = e.getCurrentItem();
        var translation = ANNIPlugin.getInstance().getTranslationManager();

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
                    player.sendMessage(translation.component(player, 
                            "gui.map_editor.set_your_location",
                            locationWithYawPitchFormat(player, player.getLocation())
                    ));
                    player.closeInventory();
                    break;
                }
                case "red_spawn": {
                    map.setSpawn(ANNITeam.RED, SpawnLocation.fromLocation(player.getLocation()));
                    player.sendMessage(translation.component(player, 
                            "gui.map_editor.set_your_location_team",
                            ANNITeam.RED.getColoredName(),
                            locationWithYawPitchFormat(player, player.getLocation())
                    ));
                    player.closeInventory();
                    break;
                }
                case "blue_spawn": {
                    map.setSpawn(ANNITeam.BLUE, SpawnLocation.fromLocation(player.getLocation()));
                    player.sendMessage(translation.component(player, 
                            "gui.map_editor.set_your_location_team",
                            ANNITeam.BLUE.getColoredName(),
                            locationWithYawPitchFormat(player, player.getLocation())
                    ));
                    player.closeInventory();
                    break;
                }
                case "green_spawn": {
                    map.setSpawn(ANNITeam.GREEN, SpawnLocation.fromLocation(player.getLocation()));
                    player.sendMessage(translation.component(player, 
                            "gui.map_editor.set_your_location_team",
                            ANNITeam.GREEN.getColoredName(),
                            locationWithYawPitchFormat(player, player.getLocation())
                    ));
                    player.closeInventory();
                    break;
                }
                case "yellow_spawn": {
                    map.setSpawn(ANNITeam.YELLOW, SpawnLocation.fromLocation(player.getLocation()));
                    player.sendMessage(translation.component(player, 
                            "gui.map_editor.set_your_location_team",
                            ANNITeam.YELLOW.getColoredName(),
                            locationWithYawPitchFormat(player, player.getLocation())
                    ));
                    player.closeInventory();
                    break;
                }
                case "red_nexus": {
                    player.closeInventory();
                    player.sendMessage(translation.component(player, "gui.map_editor.please_click_a_block"));

                    BlockBreakListener.getQueuedOnDamageMap().put(player.getUniqueId(), (bl) -> {
                        map.setNexus(ANNITeam.RED, bl.getLocation());
                        player.sendMessage(translation.component(player, 
                                "gui.map_editor.set_team_nexus_loc",
                                ANNITeam.RED.getColoredName(),
                                blockFormat(player, bl)
                        ));
                    });
                    break;
                }
                case "blue_nexus": {
                    player.closeInventory();
                    player.sendMessage(translation.component(player, "gui.map_editor.please_click_a_block"));

                    BlockBreakListener.getQueuedOnDamageMap().put(player.getUniqueId(), (bl) -> {
                        map.setNexus(ANNITeam.BLUE, bl.getLocation());
                        player.sendMessage(translation.component(player, 
                                "gui.map_editor.set_team_nexus_loc",
                                ANNITeam.BLUE.getColoredName(),
                                blockFormat(player, bl)
                        ));
                    });
                    break;
                }
                case "green_nexus": {
                    player.closeInventory();
                    player.sendMessage(translation.component(player, "gui.map_editor.please_click_a_block"));

                    BlockBreakListener.getQueuedOnDamageMap().put(player.getUniqueId(), (bl) -> {
                        map.setNexus(ANNITeam.GREEN, bl.getLocation());
                        player.sendMessage(translation.component(player, 
                                "gui.map_editor.set_team_nexus_loc",
                                ANNITeam.GREEN.getColoredName(),
                                blockFormat(player, bl)
                        ));
                    });
                    break;
                }
                case "yellow_nexus": {
                    player.closeInventory();
                    player.sendMessage(translation.component(player, "gui.map_editor.please_click_a_block"));

                    BlockBreakListener.getQueuedOnDamageMap().put(player.getUniqueId(), (bl) -> {
                        map.setNexus(ANNITeam.YELLOW, bl.getLocation());
                        player.sendMessage(translation.component(player, 
                                "gui.map_editor.set_team_nexus_loc",
                                ANNITeam.YELLOW.getColoredName(),
                                blockFormat(player, bl)
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

    private Component blockFormat(Player player, Block block) {
        var translation = ANNIPlugin.getInstance().getTranslationManager();

        return Component.text(String.format(
                translation.string(player, "format.block"),
                block.getX(),
                block.getY(),
                block.getZ()
        ));
    }

    private Component locationWithYawPitchFormat(Player player, Location location) {
        var translation = ANNIPlugin.getInstance().getTranslationManager();

        return Component.text(String.format(
                translation.string(player, "format.location.yaw_pitch"),
                location.x(), location.y(), location.z(),
                location.getYaw(), location.getPitch()
        ));
    }
}
