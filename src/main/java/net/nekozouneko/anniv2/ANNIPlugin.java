package net.nekozouneko.anniv2;

import com.google.gson.Gson;
import net.nekozouneko.anniv2.arena.ANNIArena;
import net.nekozouneko.anniv2.arena.spectator.SpectatorTask;
import net.nekozouneko.anniv2.board.BoardManager;
import net.nekozouneko.anniv2.command.*;
import net.nekozouneko.anniv2.kit.items.StunGrenade;
import net.nekozouneko.anniv2.listener.*;
import net.nekozouneko.anniv2.listener.votifier.VotifierListener;
import net.nekozouneko.anniv2.map.MapManager;
import net.nekozouneko.anniv2.message.MessageManager;
import net.nekozouneko.anniv2.util.FileUtil;
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

    public static final String LATEST_MESSAGE_VERSION = "0";
    private static ANNIPlugin plugin;

    public static ANNIPlugin getInstance() {
        return plugin;
    }

    private MessageManager messageManager;
    private BoardManager boardManager;
    private MapManager mapManager;

    private ANNIArena currentGame;
    private File defaultMapsDir;
    private Location lobby;
    private Scoreboard pluginBoard;
    private SpectatorTask spectatorTask;

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public BoardManager getBoardManager() {
        return boardManager;
    }

    public MapManager getMapManager() {
        return mapManager;
    }

    public ANNIArena getCurrentGame() {
        return currentGame;
    }

    public File getMapsDir() {
        return defaultMapsDir;
    }

    public void setLobby(Location location) {
        lobby = location.clone();
        FileUtil.writeGson(new File(getDataFolder(), "lobby.json"), location, Location.class);
    }

    public Location getLobby() {
        return lobby;
    }

    public Scoreboard getPluginBoard() {
        return pluginBoard;
    }

    @Override
    public void onLoad() {
        plugin = this;
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        defaultMapsDir = new File(getDataFolder(), "maps");
        defaultMapsDir.mkdir();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ANNIConfig.setConfig(getConfig());

        setupMessageManager();

        boardManager = new BoardManager(this);
        mapManager = new MapManager(this);

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
