package net.nekozouneko.anni;

import com.google.common.io.PatternFilenameFilter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import net.nekozouneko.anni.arena.ANNIArena;
import net.nekozouneko.anni.arena.manager.FurnaceManager;
import net.nekozouneko.anni.arena.spectator.SpectatorTask;
import net.nekozouneko.anni.board.BoardManager;
import net.nekozouneko.anni.command.*;
import net.nekozouneko.anni.command2.ChargeCommand;
import net.nekozouneko.anni.command2.CombatShopCommand;
import net.nekozouneko.anni.command2.KitCommand;
import net.nekozouneko.anni.command2.PlayerCommand;
import net.nekozouneko.anni.command2.PotionShopCommand;
import net.nekozouneko.anni.command2.SuicideCommand;
import net.nekozouneko.anni.command2.VoteCommand;
import net.nekozouneko.anni.database.Database;
import net.nekozouneko.anni.database.impl.SQLiteDatabase;
import net.nekozouneko.anni.game.save.PaperSaveDataRepository;
import net.nekozouneko.anni.game.team.PaperTeamRepository;
import net.nekozouneko.anni.game.team.TeamManager;
import net.nekozouneko.anni.item.*;
import net.nekozouneko.anni.kit.custom.CustomKitManager;
import net.nekozouneko.anni.listener.*;
import net.nekozouneko.anni.listener.votifier.VotifierListener;
import net.nekozouneko.anni.map.MapManager;
import net.nekozouneko.anni.message.MessageManager;
import net.nekozouneko.anni.message.TranslationManager;
import net.nekozouneko.anni.point.LevelManager;
import net.nekozouneko.anni.point.PaperPlayerListService;
import net.nekozouneko.anni.point.PointManager;
import net.nekozouneko.anni.task.CooldownManager;
import net.nekozouneko.anni.util.CmnUtil;
import net.nekozouneko.anni.util.FileUtil;
import net.nekozouneko.anni.util.VaultUtil;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.*;

public final class ANNIPlugin extends JavaPlugin {

    public static final String LATEST_MESSAGE_VERSION = "17";
    @Getter
    private static ANNIPlugin instance;

    @Getter
    private MessageManager messageManager;
    @Getter
    private BoardManager boardManager;
    @Getter
    private MapManager mapManager;
    @Getter
    private CooldownManager cooldownManager;
    @Getter
    private Database database;
    @Getter
    private LevelManager levelManager;
    @Getter
    private PointManager pointManager;
    @Getter
    private FurnaceManager furnaceManager;

    @Getter
    private ANNIArena currentGame;
    @Getter
    private File defaultMapsDir;
    @Getter
    private File defaultKitsDir;
    @Getter
    private File defaultLangDir;
    @Getter
    private Location lobby;
    @Getter
    private Scoreboard pluginBoard;
    private SpectatorTask spectatorTask;
    @Getter
    private CustomKitManager customKitManager;
    @Getter
    private TranslationManager translationManager;

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

        defaultLangDir = new File(getDataFolder(), "lang");
        defaultLangDir.mkdir();
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        ANNIConfig.setConfig(getConfig());

        try {
            database = new SQLiteDatabase(new File(getDataFolder(), "database.db").toString());
        }
        catch (SQLException sql) {
            sql.printStackTrace();
            setEnabled(false);
            return;
        }

        FurnaceManager.VirtualFurnace.reloadRecipes();

        setupMessageManager();
        reloadTranslationManager();

        boardManager = new BoardManager(this);
        mapManager = new MapManager(this);
        customKitManager = new CustomKitManager(this);
        levelManager = new LevelManager();
        pointManager = new PointManager();

        mapManager.load(defaultMapsDir);

        cooldownManager = new CooldownManager();
        cooldownManager.runTaskTimer(this, 0, 5);

        try {
            lobby = FileUtil.readGson(new File(getDataFolder(), "lobby.json"), Location.class);
        }
        catch (Exception e) {
            e.printStackTrace();
            lobby = null;
            getLogger().warning("Lobby is not set!");
        }

        pluginBoard = getServer().getScoreboardManager().getNewScoreboard();

        getServer().getPluginManager().registerEvents(new AsyncPlayerChatListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDenyActionListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerPortalListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnListener(), this);
        getServer().getPluginManager().registerEvents(new ProjectileLaunchListener(), this);
        getServer().getPluginManager().registerEvents(new BlockPistonListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new EnchantItemListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerExpChargeListener(), this);

        if (!VaultUtil.hasEco()) {
            setEnabled(false);
            return;
        }

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
        getServer().getPluginManager().registerEvents(new GrapplingHook(), this);
        getServer().getPluginManager().registerEvents(new DefenseArtifact(), this);
        getServer().getPluginManager().registerEvents(new NexusCompass(), this);
        getServer().getPluginManager().registerEvents(new EnderFurnace(), this);
        getServer().getPluginManager().registerEvents(new Swapper(), this);

        furnaceManager = new FurnaceManager();
        furnaceManager.runTaskTimer(this, 0, 1);

        var teamManager = new TeamManager(new PaperTeamRepository(pluginBoard));

        currentGame = new ANNIArena(this, teamManager, new PaperSaveDataRepository(teamManager), new PaperPlayerListService(levelManager));
        spectatorTask = new SpectatorTask();
        currentGame.runTaskTimer(this, 0, 20);
        spectatorTask.runTaskTimer(this, 0, 20);

        getCommand("anni-admin").setExecutor(new ANNIAdminCommand());
        getCommand("anni").setExecutor(new ANNICommand());

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(ChargeCommand.get(Commands.literal("charge")).build());
            event.registrar().register(CombatShopCommand.get(Commands.literal("combat-shop")).build());
            event.registrar().register(KitCommand.get(Commands.literal("kit")).build());
            event.registrar().register(PlayerCommand.get(Commands.literal("player")).build());
            event.registrar().register(PotionShopCommand.get(Commands.literal("potion-shop")).build());
            event.registrar().register(SuicideCommand.get(Commands.literal("suicide")).build());
            event.registrar().register(VoteCommand.get(Commands.literal("vote")).build());
        });

        registerRecipe();
    }

    @Override
    public void onDisable() {
        if (!currentGame.isCancelled()) currentGame.cancel();
        unregisterRecipe();

        if (database != null) database.close();

        instance = null;
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

    private void reloadTranslationManager() {
        List<String> languageFiles = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(getTextResource("languages.txt"))) {
            String line = reader.readLine();
            while (line != null && !line.isEmpty()) {
                languageFiles.add(line);
                line = reader.readLine();
            }
        }
        catch (IOException io) {
            io.printStackTrace();
        }

        Gson gson = FileUtil.createGson();

        Map<Locale, Map<String, String>> messages = new HashMap<>();

        for (String languageFile : languageFiles) {
            getLogger().info("Loading translation: " + languageFile);
            Map<String, String> languageMessages = new HashMap<>();
            JsonObject obj = gson.fromJson(getTextResource(languageFile), JsonObject.class);

            obj.entrySet().forEach(entry -> {
                languageMessages.put(entry.getKey(), entry.getValue().getAsString());
            });

            messages.put(CmnUtil.localeCodeToLocale(obj.get("meta.lang.id").getAsString()), languageMessages);
        }

        for (File file : defaultLangDir.listFiles(new PatternFilenameFilter(".+\\.json"))) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                JsonObject obj = gson.fromJson(reader, JsonObject.class);
                Locale tempLocale = CmnUtil.localeCodeToLocale(obj.get("meta.lang.id").getAsString());
                Map<String, String> temp = messages.getOrDefault(tempLocale, new HashMap<>());

                obj.entrySet().forEach(entry -> {
                        temp.put(entry.getKey(), entry.getValue().getAsString());
                });

                messages.put(tempLocale, temp);
            }
            catch (IOException io) {
                io.printStackTrace();
            }
        }

        translationManager = new TranslationManager(ANNIConfig.getDefaultLocale(), messages);
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
