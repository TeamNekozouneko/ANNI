package net.nekozouneko.anni.kit;

import net.kyori.adventure.text.Component;
import net.nekozouneko.anni.ANNIConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;

public interface Kit {

    String getId();

    Component getName(Locale locale);

    default Component getName() {
        return getName(ANNIConfig.getDefaultLocale());
    }

    default Component getName(Player player) {
        return getName(player.locale());
    }

    List<Component> getLore(Locale locale);

    default List<Component> getLore(Player player) {
        return getLore(player.locale());
    }

    default List<Component> getLore() {
        return getLore(ANNIConfig.getDefaultLocale());
    }

    String getShortName();

    Material getIcon();

    ItemStack[] getKitContents(Locale locale);

    default void onDeath(Player player) {}

    default void onRespawn(Player player) {}

}
