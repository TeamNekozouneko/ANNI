package net.nekozouneko.anniv2;

import com.google.gson.Gson;
import net.nekozouneko.anniv2.arena.ArenaManager;
import net.nekozouneko.anniv2.board.BoardManager;
import net.nekozouneko.anniv2.command.ANNIAdminCommand;
import net.nekozouneko.anniv2.command.ANNICommand;
import net.nekozouneko.anniv2.listener.PlayerJoinListener;
import net.nekozouneko.anniv2.listener.PlayerQuitListener;
import net.nekozouneko.anniv2.map.MapManager;
import net.nekozouneko.anniv2.message.MessageManager;
import net.nekozouneko.anniv2.util.FileUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
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
    private ArenaManager arenaManager;
    private MapManager mapManager;

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public BoardManager getBoardManager() {
        return boardManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public MapManager getMapManager() {
        return mapManager;
    }

    @Override
    public void onLoad() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File mapsd = new File(getDataFolder(), "maps");
        mapsd.mkdir();
    }

    @Override
    public void onEnable() {
        plugin = this;

        setupMessageManager();

        boardManager = new BoardManager(this);
        arenaManager = new ArenaManager(this);
        mapManager = new MapManager();

        mapManager.load(new File(getDataFolder(), "maps"));

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);

        arenaManager.create().runTaskTimer(this, 0, 20);

        getCommand("anni-admin").setExecutor(new ANNIAdminCommand());
        getCommand("anni").setExecutor(new ANNICommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
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
