package net.nekozouneko.anniv2.util;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import com.sk89q.worldedit.math.BlockVector3;
import net.nekozouneko.anniv2.arena.team.ANNITeam;
import net.nekozouneko.anniv2.gson.BlockVector3Deserializer;
import net.nekozouneko.anniv2.gson.EnumMapInstanceCreator;
import net.nekozouneko.anniv2.gson.LocationInstanceCreator;
import net.nekozouneko.anniv2.map.Nexus;
import net.nekozouneko.anniv2.map.SpawnLocation;
import net.nekozouneko.commons.lang.io.Files2;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

public final class FileUtil {

    private static final List<String> DO_NOT_COPY_WORLD_FILE = Arrays.asList("uid.dat", "session.lock");

    private FileUtil() {
    }

    public static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(
                        EnumMap.class,
                        new EnumMapInstanceCreator<ANNITeam, SpawnLocation>(ANNITeam.class)
                )
                .registerTypeAdapter(
                        EnumMap.class,
                        new EnumMapInstanceCreator<ANNITeam, Nexus>(ANNITeam.class)
                )
                .registerTypeAdapter(
                        BlockVector3.class,
                        new BlockVector3Deserializer()
                )
                .registerTypeAdapter(
                        Location.class,
                        new LocationInstanceCreator()
                )
                .serializeNulls()
                .disableHtmlEscaping()
                .create();
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
        } catch (IOException e) {
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
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean checkFileNameValidate(String name) {
        return !name.matches(".*[\\\\/:*?\"<>|].*");
    }

    public static World copyWorld(World original, String name) throws IOException {
        Path from = Paths.get(original.getName());
        Path to = Paths.get(name);

        Preconditions.checkArgument(!checkFileNameValidate(name), "Cant use character detected");
        Preconditions.checkState(Bukkit.getWorld(name) != null, "World \"" + name + "\" is already exists");
        if (Files.exists(to)) throw new FileAlreadyExistsException(name + " is already exists");

        Files.walkFileTree(from, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path dir2 = to.resolve(from.relativize(dir));
                if (!Files.exists(dir2)) Files.createDirectory(dir2);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path file2 = to.resolve(from.relativize(file));

                if (!(Files.exists(file2) || DO_NOT_COPY_WORLD_FILE.contains(file2.getFileName().toString()))) {
                    Files.copy(file, file2, StandardCopyOption.COPY_ATTRIBUTES);
                }

                return FileVisitResult.CONTINUE;
            }
        });

        return WorldCreator.name(name).createWorld();
    }

    public static void deleteWorld(World w) throws IOException {
        File fold = w.getWorldFolder();
        Bukkit.unloadWorld(w, false);
        Files2.deleteIfExists(fold.toPath());
    }

}
