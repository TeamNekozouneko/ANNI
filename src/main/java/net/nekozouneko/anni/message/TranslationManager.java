package net.nekozouneko.anni.message;

import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

import java.util.*;

@AllArgsConstructor
public class TranslationManager {

    private final Locale defaultLocale;
    private final Map<Locale, Map<String, String>> data;

    public Component component(String key, Object... args) {
        return component(defaultLocale, key, args);
    }

    public Component component(Player player, String key, Object... args) {
        return component(player.locale(), key, args);
    }

    public Component component(Locale locale, String key, Object... args) {
        String message = rawString(locale, key);

        if (message.isEmpty()) return Component.empty();

        List<TagResolver> argResolvers = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];

            if (arg instanceof ComponentLike clike) {
                argResolvers.add(Placeholder.component("" + i, clike));
            }
            else argResolvers.add(Placeholder.unparsed("" + i, arg.toString()));
        }

        return MiniMessage.miniMessage().deserialize(message, argResolvers.toArray(new TagResolver[0]));
    }

    public Component componentLines(String key, Object... args) {
        return componentLines(defaultLocale, key, args);
    }

    public Component componentLines(Player player, String key, Object... args) {
        return componentLines(player.locale(), key, args);
    }

    public Component componentLines(Locale locale, String key, Object... args) {
        int i = 0;
        TextComponent.Builder component = Component.text();

        if (!isMessageExists(locale, key + "." + i)) throw new IllegalArgumentException(String.format("The message [%s] is not defined", key + "." + i));
        Component comp = component(locale, key + "." + i, args);
        while (comp != null) {
            if (i != 0) component.append(Component.newline());
            component.append(comp);

            i++;
            if (!isMessageExists(locale, key + "." + i)) break;
            comp = component(locale, key + "." + i, args);
        }

        return component.asComponent();
    }

    public List<Component> componentList(String key, Object... args) {
        return componentList(defaultLocale, key, args);
    }

    public List<Component> componentList(Player player, String key, Object... args) {
        return componentList(player.locale(), key, args);
    }

    public List<Component> componentList(Locale locale, String key, Object... args) {
        int i = 0;
        List<Component> result = new ArrayList<>();

        if (!isMessageExists(locale, key + "." + i)) throw new IllegalArgumentException(String.format("The message [%s] is not defined", key + "." + i));
        Component comp = component(locale, key + "." + i, args);
        while (comp != null) {
            if (i != 0) result.add(Component.newline());
            result.add(comp);

            i++;
            if (!isMessageExists(locale, key + "." + i)) break;
            comp = component(locale, key + "." + i, args);
        }

        return result;
    }

    public String string(String key, Object... args) {
        return string(defaultLocale, key, args);
    }

    public String string(Player player, String key, Object... args) {
        return string(player.locale(), key, args);
    }

    public String string(Locale locale, String key, Object... args) {
        String message = rawString(locale, key);

        if (args != null && args.length != 0) {
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg == null) arg = "";

                message = message.replace("{" + i + "}", Objects.toString(arg));
            }
        }

        return message;
    }

    protected String rawString(Locale locale, String key) {
        if (!data.containsKey(locale)) locale = defaultLocale;

        Map<String, String> messages = new HashMap<>();
        if (locale != defaultLocale) messages.putAll(data.get(defaultLocale));
        messages.putAll(data.get(locale));

        String message = messages.get(key);

        if (message == null) throw new IllegalArgumentException(String.format("The message [%s] is not defined", key));

        return message;
    }

    public boolean isMessageExists(Locale locale, String key) {
        if (!data.containsKey(locale)) locale = defaultLocale;

        Map<String, String> messages = new HashMap<>();
        if (locale != defaultLocale) messages.putAll(data.get(defaultLocale));
        messages.putAll(data.get(locale));

        String message = messages.get(key);

        return message != null;
    }

}
