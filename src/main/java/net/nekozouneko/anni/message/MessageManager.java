package net.nekozouneko.anni.message;

import com.sk89q.worldedit.math.BlockVector3;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.nekozouneko.anni.map.SpawnLocation;
import net.nekozouneko.anni.util.CmnUtil;
import org.bukkit.Location;

import java.util.*;

public class MessageManager {

    protected Map<String, String> map;

    public MessageManager(Map<String, String> map) {
        this.map = map;
    }

    //@Deprecated(forRemoval = true, since = "4.0")
    public String build(String key, Object... args) {
        if (!map.containsKey(key))
            throw new RuntimeException("Message of key '" + key + "' is not defined.");

        String s = map.get(key);

        if (args != null && args.length != 0) {
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg == null) arg = "";
                if (arg instanceof ComponentLike) arg = LegacyComponentSerializer.legacySection().serialize(((ComponentLike) arg).asComponent());

                s = s != null ? s.replace("{" + i + "}", Objects.toString(arg)) : "";
            }
        }

        return CmnUtil.replaceColorCode(s);
    }

    //@Deprecated(forRemoval = true, since = "4.0")
    public List<String> buildList(String key, Object... args) {
        List<String> l = new ArrayList<>();

        for (int i = 0; true; i++) {
            if (map.containsKey(key + "." + i)) {
                l.add(build(key + "." + i, args));
            }
            else break;
        }

        return l;
    }

    //@Deprecated(forRemoval = true, since = "4.0")
    public String[] buildArray(String key, Object... args) {
        return buildList(key, args).toArray(new String[0]);
    }

    //@Deprecated(forRemoval = true, since = "4.0")
    public String buildLines(String key, Object... args) {
        return String.join("\n", buildList(key, args));
    }

    public String[] buildBigChar(char chara, String color, Object... args) {
        List<Object> l = new ArrayList<>();
        for (Object obj : args) {
            if (obj instanceof String s) {
                l.addAll(Arrays.asList(s.split("\n")));
            }
            else if (obj instanceof List<?> list) {
                l.addAll(list);
            }
            else l.add(obj);
        }
        args = l.toArray();

        return buildList("big." + chara, args).stream()
                .map(str -> str.replace("&-", (color != null || !color.isEmpty()) ? "§" + color : ""))
                .toArray(String[]::new);
    }

    public String blockLocationFormat(BlockVector3 loc) {
        String format = build("xyz.blockformat");
        return build(
                "xyz",
                String.format(format, loc.x()),
                String.format(format, loc.y()),
                String.format(format, loc.z())
        );
    }

    public String yawPitchLocationFormat(Location loc) {
        String format = build("xyz.format");
        String ypformat = build("yawpitch.format");
        return build(
                "xyzyawpitch",
                String.format(format, loc.getX()),
                String.format(format, loc.getY()),
                String.format(format, loc.getZ()),
                String.format(ypformat, loc.getYaw()),
                String.format(ypformat, loc.getPitch())
        );
    }

    public String yawPitchLocationFormat(SpawnLocation sloc) {
        return yawPitchLocationFormat(sloc.toLocation(null));
    }

    public String getVersion() {
        return map.get("version");
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

}
