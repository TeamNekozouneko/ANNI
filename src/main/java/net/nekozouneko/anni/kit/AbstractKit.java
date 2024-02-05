package net.nekozouneko.anni.kit;

import com.google.common.base.Enums;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import net.nekozouneko.anni.ANNIPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractKit {

    @Getter
    protected final String id;
    @Getter @Setter
    protected String shortName;
    @Setter
    protected String name;

    protected String icon;
    @SerializedName("description")
    protected List<String> lore;

    protected AbstractKit(String id, String shortName, String name, String icon, List<String> lore) {
        this.id = id;
        this.shortName = shortName;
        this.name = name;
        this.icon = icon;
        this.lore = lore != null ? new ArrayList<>(lore) : new ArrayList<>();
    }

    public String getName() {
        return ANNIPlugin.getInstance().getMessageManager().build(name);
    }

    public Material getIcon() {
        return Enums.getIfPresent(Material.class, icon).or(Material.CHEST);
    }

    public List<String> getLore() {
        return Collections.unmodifiableList(lore);
    }

    public abstract ItemStack[] getKitContents();

}
