package net.nekozouneko.anniv2.message;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import net.nekozouneko.anniv2.map.SpawnLocation;
import net.nekozouneko.anniv2.util.CmnUtil;

import org.bukkit.Location;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageManager {

    protected Map<String, String> map;

    public MessageManager(Map<String, String> map) {
        this.map = map;
    }

    public String build(String key, Object... args) {
        String s = map.get(key);

        if (s == null)
            throw new RuntimeException("Message of key '" + key + "' is not defined.");

        if (args != null && args.length != 0) {
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg == null) continue;

                if (arg instanceof Integer) {
                    int numb = (int) arg;

                    Pattern p = Pattern.compile("(\\{" + i + "\\|(.+)})");
                    Matcher m = p.matcher(s);

                    if (m.find()) {
                        String format1 = m.group(1);
                        String args1 = m.group(2);
                        String[] args2 = args1.split("(?!\\\\)\\|");

                        s = s.replace(format1, args2[numb]);
                    }
                }
                else s = s.replace("{" + i + "}", Objects.toString(arg));
            }
        }

        return CmnUtil.replaceColorCode(s);
    }

    public List<String> buildList(String key, Object... args) {
        List<String> l = new ArrayList<>();

        for (int i = 0; true; i++) {
            if (map.get(key + "." + i) != null) {
                l.add(build(key + "." + i, args));
            }
            else break;
        }

        return l;
    }

    public String buildLines(String key, Object... args) {
        return String.join("\n", buildList(key, args));
    }

    public String blockLocationFormat(BlockVector3 loc) {
        String format = build("xyz.blockformat");
        return build(
                "xyz",
                String.format(format, loc.getBlockX()),
                String.format(format, loc.getBlockY()),
                String.format(format, loc.getBlockZ())
        );
    }

    public String blockLocationFormat(Location loc) {
        return blockLocationFormat(BukkitAdapter.asBlockVector(loc));
    }

    public String locationFormat(Location loc) {
        String format = build("xyz.format");
        return build(
                "xyz",
                String.format(format, loc.getX()),
                String.format(format, loc.getY()),
                String.format(format, loc.getZ())
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
