package net.nekozouneko.anni.kit;

import com.google.common.base.Enums;
import com.google.gson.annotations.SerializedName;
import net.nekozouneko.anni.ANNIPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbsANNIKit {

    protected final String id;
    protected String shortName;
    protected String name;

    protected String icon;
    @SerializedName("description")
    protected List<String> lore;

    protected AbsANNIKit(String id, String shortName, String name, String icon, List<String> lore) {
        this.id = id;
        this.shortName = shortName;
        this.name = name;
        this.icon = icon;
        this.lore = lore != null ? new ArrayList<>(lore) : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getName() {
        return ANNIPlugin.getInstance().getMessageManager().build(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public Material getIcon() {
        return Enums.getIfPresent(Material.class, icon).or(Material.CHEST);
    }

    public List<String> getLore() {
        return Collections.unmodifiableList(lore);
    }

    public abstract ItemStack[] getKitContents();

}
