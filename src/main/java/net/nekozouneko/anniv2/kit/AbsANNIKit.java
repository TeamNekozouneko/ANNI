package net.nekozouneko.anniv2.kit;

import net.nekozouneko.anniv2.ANNIPlugin;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class AbsANNIKit {

    protected final String id;
    protected final String shortName;
    protected final String name;

    protected AbsANNIKit(String id, String shortName, String name) {
        this.id = id;
        this.shortName = shortName;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getShortName() {
        return shortName;
    }

    public String getName() {
        return ANNIPlugin.getInstance().getMessageManager().build(name);
    }

    public abstract ItemStack[] getKitContents();

    public abstract List<String> getBlackList();

}
