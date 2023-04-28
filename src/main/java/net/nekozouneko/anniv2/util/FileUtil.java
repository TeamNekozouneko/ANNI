package net.nekozouneko.anniv2.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import net.nekozouneko.anniv2.arena.team.ANNITeam;
import net.nekozouneko.anniv2.gson.EnumMapInstanceCreator;
import net.nekozouneko.anniv2.map.Nexus;
import net.nekozouneko.anniv2.map.SpawnLocation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;

public final class FileUtil {

    private FileUtil() {}

    public static Gson createGson() {
        GsonBuilder gb = new GsonBuilder()
                .registerTypeAdapter(
                        EnumMap.class,
                        new EnumMapInstanceCreator<ANNITeam, SpawnLocation>(ANNITeam.class)
                )
                .registerTypeAdapter(
                        EnumMap.class,
                        new EnumMapInstanceCreator<ANNITeam, Nexus>(ANNITeam.class)
                )
                .serializeNulls();

        return gb.create();
    }

    public static <T> boolean writeGson(File to, T obj, Class<T> cls) {
        return writeGson(createGson(), to, obj, cls);
    }

    public static <T> boolean writeGson(Gson gson, File to, T obj, Class<T> cls) {
        try (
                JsonWriter w = new JsonWriter(new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(to),
                                StandardCharsets.UTF_8
                        )
                ))
        ) {
            w.setIndent("    ");
            gson.toJson(obj, cls, w);
            w.flush();
        }
        catch (IOException e) {
            return false;
        }

        return true;
    }

    public static <T> T readGson(File from, Class<T> cls) {
        return readGson(createGson(), from, cls);
    }

    public static <T> T readGson(Gson gson, File from, Class<T> cls) {
        try (
                BufferedReader r = new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream(from),
                                StandardCharsets.UTF_8
                        )
                )
        ) {
            return gson.fromJson(r, cls);
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
