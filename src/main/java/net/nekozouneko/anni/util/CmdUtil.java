package net.nekozouneko.anni.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class CmdUtil {

    private CmdUtil() { throw new ExceptionInInitializerError(); }

    public static List<String> simpleTabComplete(String in, String... suggest) {
        return simpleTabComplete(in, Arrays.asList(suggest));
    }

    public static List<String> simpleTabComplete(String in, Collection<String> suggest) {
        return suggest.stream()
                .filter((s) -> s.toLowerCase().startsWith(in.toLowerCase()))
                .collect(Collectors.toList());
    }

}
