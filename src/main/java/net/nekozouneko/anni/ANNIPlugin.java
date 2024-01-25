package net.nekozouneko.anni;

import com.google.gson.Gson;
import lombok.Getter;
import net.nekozouneko.anni.arena.ANNIArena;
import net.nekozouneko.anni.arena.spectator.SpectatorTask;
import net.nekozouneko.anni.board.BoardManager;
import net.nekozouneko.anni.command.*;
import net.nekozouneko.anni.kit.custom.CustomKitManager;
import net.nekozouneko.anni.kit.items.AirJump;
import net.nekozouneko.anni.kit.items.StunGrenade;
import net.nekozouneko.anni.listener.*;
import net.nekozouneko.anni.listener.votifier.VotifierListener;
import net.nekozouneko.anni.map.MapManager;
import net.nekozouneko.anni.message.MessageManager;
import net.nekozouneko.anni.util.FileUtil;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public final class ANNIPlugin extends JavaPlugin {

    public static final String LATEST_MESSAGE_VERSION = "5";
    private static ANNIPlugin plugin;

    public static ANNIPlugin getInstance() {
        return plugin;
    }

    @Getter
    private MessageManager messageManager;
    @Getter
    private BoardManager boardManager;
    @Getter
    private MapManager mapManager;

    @Getter
    private ANNIArena currentGame;
    private File defaultMapsDir;
    private File defaultKitsDir;
    @Getter
    private Location lobby;
    @Getter
    private Scoreboard pluginBoard;
    private SpectatorTask spectatorTask;
    @Getter
    private CustomKitManager customKitManager;

    public File getMapsDir() {
        return defaultMapsDir;
    }

    public File getKitsDir() {
        return defaultKitsDir;
    }

    public void setLobby(Location location) {
        lobby = location.clone();
        FileUtil.writeGson(new File(getDataFolder(), "lobby.json"), location, Location.class);
    }

    @Override
    public void onLoad() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        defaultMapsDir = new File(getDataFolder(), "maps");
        defaultMapsDir.mkdir();

        defaultKitsDir = new File(getDataFolder(), "kits");
        defaultKitsDir.mkdir();
    }

    @Override
    public void onEnable() {
        plugin = this;

        saveDefaultConfig();
        ANNIConfig.setConfig(getConfig());

        setupMessageManager();

        boardManager = new BoardManager(this);
        mapManager = new MapManager(this);
        customKitManager = new CustomKitManager(this);

        mapManager.load(defaultMapsDir);

        lobby = FileUtil.readGson(new File(getDataFolder(), "lobby.json"), Location.class);

        pluginBoard = getServer().getScoreboardManager().getNewScoreboard();

        getServer().getPluginManager().registerEvents(new AsyncPlayerChatListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDenyActionListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnListener(), this);

        if (ANNIConfig.isVotifierVoteEnabled()) {
            try {
                Class.forName("com.vexsoftware.votifier.model.VotifierEvent");
                getServer().getPluginManager().registerEvents(new VotifierListener(), this);
            } catch (ClassNotFoundException e) {
                getLogger().info("Votifier event class is not defined.");
            }
        }

        getServer().getPluginManager().registerEvents(new StunGrenade(), this);
        getServer().getPluginManager().registerEvents(new AirJump(), this);

        currentGame = new ANNIArena(this, "current");
        spectatorTask = new SpectatorTask();
        currentGame.runTaskTimer(this, 0, 20);
        spectatorTask.runTaskTimer(this, 0, 1);

        getCommand("anni-admin").setExecutor(new ANNIAdminCommand());
        getCommand("anni").setExecutor(new ANNICommand());
        getCommand("combat-shop").setExecutor(new CombatShopCommand());
        getCommand("potion-shop").setExecutor(new PotionShopCommand());
        getCommand("suicide").setExecutor(new SuicideCommand());
        getCommand("vote").setExecutor(new VoteCommand());
        getCommand("kit").setExecutor(new KitCommand());
        getCommand("point").setExecutor(new PointCommand());

        registerRecipe();
    }

    @Override
    public void onDisable() {
        if (!currentGame.isCancelled()) currentGame.cancel();
        unregisterRecipe();
    }

    @SuppressWarnings("unchecked")
    private void setupMessageManager() {
        try (InputStreamReader lr = new InputStreamReader(getResource("messages.json"), StandardCharsets.UTF_8)) {
            Map<String, String> messageMap = new HashMap<>();

            Gson gson = FileUtil.createGson();

            Map<String, String> latestMap = gson.fromJson(lr, Map.class);
            messageMap.putAll(latestMap);

            File mf = new File(getDataFolder(), "messages.json");

            if (!mf.exists())
                Files.copy(getResource("messages.json"), mf.toPath());

            Map<String, String> curr = FileUtil.readGson(mf, Map.class);

            if (!curr.get("version").equals(LATEST_MESSAGE_VERSION)) {
                String lmf = "messages.json." + curr.get("version");
                Files.copy(
                        mf.toPath(), new File(getDataFolder(), lmf).toPath(),
                        StandardCopyOption.COPY_ATTRIBUTES,
                        StandardCopyOption.REPLACE_EXISTING
                );
                FileUtil.writeGson(mf, latestMap, Map.class);
                getLogger().info("Updated messages.json! Saved backup to " + lmf);
            }
            else messageMap.putAll(curr);

            messageManager = new MessageManager(messageMap);
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            getLogger().warning("Failed to load messages");

            setEnabled(false);
        }
    }

    private void registerRecipe() {
        NamespacedKey g2f = new NamespacedKey(this, "flint");
        NamespacedKey ega = new NamespacedKey(this, "enchanted_golden_apple");

        if (getServer().getRecipe(g2f) == null) {
            getServer().addRecipe(
                    new ShapelessRecipe(
                            g2f, ItemStackBuilder.of(Material.FLINT).build()
                    ).addIngredient(Material.GRAVEL)
            );
        }
        if (getServer().getRecipe(ega) == null) {
            getServer().addRecipe(
                    new ShapedRecipe(ega, ItemStackBuilder.of(Material.ENCHANTED_GOLDEN_APPLE).build())
                            .shape(
                                    "GGG",
                                    "GAG",
                                    "GGG"
                            )
                            .setIngredient('G', Material.GOLD_BLOCK)
                            .setIngredient('A', Material.APPLE)
            );
        }
    }

    private void unregisterRecipe() {
        NamespacedKey g2f = new NamespacedKey(this, "flint");
        NamespacedKey ega = new NamespacedKey(this, "enchanted_golden_apple");

        if (getServer().getRecipe(g2f) != null)
            getServer().removeRecipe(g2f);
        if (getServer().getRecipe(ega) != null)
            getServer().removeRecipe(ega);
    }
}
