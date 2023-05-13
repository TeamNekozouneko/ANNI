package net.nekozouneko.anniv2;

import com.google.gson.Gson;
import net.nekozouneko.anniv2.arena.ANNIArena;
import net.nekozouneko.anniv2.board.BoardManager;
import net.nekozouneko.anniv2.command.ANNIAdminCommand;
import net.nekozouneko.anniv2.command.ANNICommand;
import net.nekozouneko.anniv2.listener.BlockBreakListener;
import net.nekozouneko.anniv2.listener.BlockPlaceListener;
import net.nekozouneko.anniv2.listener.PlayerJoinListener;
import net.nekozouneko.anniv2.listener.PlayerQuitListener;
import net.nekozouneko.anniv2.map.MapManager;
import net.nekozouneko.anniv2.message.MessageManager;
import net.nekozouneko.anniv2.util.FileUtil;
import org.bukkit.Location;
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
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        defaultMapsDir = new File(getDataFolder(), "maps");
        defaultMapsDir.mkdir();
    }

    @Override
    public void onEnable() {
        plugin = this;

        setupMessageManager();

        boardManager = new BoardManager(this);
        mapManager = new MapManager(this);

        mapManager.load(defaultMapsDir);

        lobby = FileUtil.readGson(new File(getDataFolder(), "lobby.json"), Location.class);

        pluginBoard = getServer().getScoreboardManager().getNewScoreboard();

        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);

        currentGame = new ANNIArena(this, "current");
        currentGame.runTaskTimer(this, 0, 20);

        getCommand("anni-admin").setExecutor(new ANNIAdminCommand());
        getCommand("anni").setExecutor(new ANNICommand());
    }

    @Override
    public void onDisable() {
        if (!currentGame.isCancelled()) currentGame.cancel();
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
}
