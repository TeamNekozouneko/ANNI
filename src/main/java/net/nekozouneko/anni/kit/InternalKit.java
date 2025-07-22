package net.nekozouneko.anni.kit;

import net.kyori.adventure.text.Component;
import net.nekozouneko.anni.ANNIPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;

public abstract class InternalKit implements Kit {

    private final String id, nameKey, descriptionKey, shortName;
    private final Material icon;

    protected InternalKit(String id, String nameKey, String descriptionKey, String shortName, Material icon) {
        this.id = id;
        this.nameKey = nameKey;
        this.descriptionKey = descriptionKey;
        this.shortName = shortName;
        this.icon = icon;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Component getName(Locale locale) {
        return ANNIPlugin.getInstance().getTranslationManager().component(nameKey);
    }

    @Override
    public List<Component> getLore(Locale locale) {
        return ANNIPlugin.getInstance().getTranslationManager().componentList(descriptionKey);
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public Material getIcon() {
        return icon;
    }

    @Override
    public ItemStack[] getKitContents() {
        return new ItemStack[0];
    }
}
