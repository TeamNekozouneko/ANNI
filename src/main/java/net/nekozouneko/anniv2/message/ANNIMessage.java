package net.nekozouneko.anniv2.message;

import net.nekozouneko.anniv2.util.CmnUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ANNIMessage {

    protected Map<String, String> map;

    public ANNIMessage(Map<String, String> map) {
        this.map = map;
    }

    public String build(String key, Object... args) {
        String s = map.get(key);

        if (s == null)
            throw new RuntimeException("Message of key '" + key + "' is not defined.");

        if (args != null && args.length != 0) {
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];

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

    public String getVersion() {
        return map.get("version");
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

}
