package net.nekozouneko.anni.kit;

import net.kyori.adventure.text.Component;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.item.NexusCompass;
import net.nekozouneko.anni.util.CmnUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

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
    public ItemStack[] getKitContents(Locale locale) {
        ItemStack[] inv = new ItemStack[41];

        inv[0] = ItemStack.of(Material.WOODEN_SWORD);
        inv[1] = ItemStack.of(Material.STONE_PICKAXE);
        inv[2] = ItemStack.of(Material.STONE_AXE);
        inv[3] = ItemStack.of(Material.STONE_SHOVEL);

        inv[7] = ItemStack.of(Material.BREAD).add(15);
        inv[8] = NexusCompass.get(locale);
        CmnUtil.editPDC(inv[8], c -> c.set(new NamespacedKey(ANNIPlugin.getInstance(), "no-remove"), PersistentDataType.INTEGER, 1));

        inv[39] = ItemStack.of(Material.LEATHER_HELMET);
        inv[38] = ItemStack.of(Material.LEATHER_CHESTPLATE);
        inv[37] = ItemStack.of(Material.LEATHER_LEGGINGS);
        inv[36] = ItemStack.of(Material.LEATHER_BOOTS);

        return inv;
    }
}
