package net.nekozouneko.anniv2.map;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import net.nekozouneko.anniv2.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MapManager {

    private final Map<String, ANNIMap> maps = new HashMap<>();

    public void load(File f) {
        Preconditions.checkArgument(f != null && f.exists());

        if (f.isDirectory()) {
            for (File f2 : f.listFiles()) {
                if (f2.getName().endsWith(".json") && f2.isFile()) load(f2);
            }
        }
        else {
            Gson gson = FileUtil.createGson();
            try (Reader r = Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8)) {
                ANNIMap mp = gson.fromJson(r, ANNIMap.class);
                maps.put(mp.getId(), mp);

            }
            catch (IOException e) { e.printStackTrace(); }
        }
    }

    public void unload(String id) {
        maps.remove(id);
    }

    public void unload(ANNIMap map) {
        maps.keySet().forEach((s) -> {
            if (maps.get(s).equals(map)) maps.remove(s);
        });
    }

    public ANNIMap getMap(String id) {
        return maps.get(id);
    }

    public Collection<ANNIMap> getMaps() {
        return Collections.unmodifiableCollection(maps.values());
    }

}
