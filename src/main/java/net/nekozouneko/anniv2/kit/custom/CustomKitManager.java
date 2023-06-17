package net.nekozouneko.anniv2.kit.custom;

import com.google.common.base.Preconditions;
import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.util.FileUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CustomKitManager {

    private final ANNIPlugin plugin;
    private final Map<String, CustomKit> kits = new HashMap<>();

    public CustomKitManager(ANNIPlugin plugin) {
        this.plugin = plugin;
        loadDir(plugin.getKitsDir());
    }

    public void load(File f) {
        try (
                InputStreamReader in = new InputStreamReader(
                        new FileInputStream(f),
                        StandardCharsets.UTF_8
                );
                BufferedReader read = new BufferedReader(in)
        ) {
            CustomKit kit = FileUtil.createGson().fromJson(read, CustomKit.class);
            if (kit != null) kits.put(kit.getId(), kit);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadDir(File dir) {
        Preconditions.checkArgument(dir != null && dir.isDirectory());

        for (File f : dir.listFiles((dir1, name) -> name.endsWith(".json"))) {
            if (f == null) continue;
            load(f);
        }
    }

    public void unload(String id) {
        kits.remove(id);
    }

    public void unload(CustomKit kit) {
        new HashSet<>(kits.keySet()).forEach(key -> {
                if (kits.get(key).equals(kit)) {
                    kits.remove(key);
                }
        });
    }

    public void reload() {
        kits.clear();
        loadDir(plugin.getKitsDir());
    }

    public CustomKit getKit(String id) {
        return kits.get(id);
    }

    public List<CustomKit> getKits() {
        return Collections.unmodifiableList(new ArrayList<>(kits.values()));
    }

}
