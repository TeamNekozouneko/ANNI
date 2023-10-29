package net.nekozouneko.anni.kit.custom;

import com.google.common.base.Enums;
import com.google.common.base.Preconditions;
import net.nekozouneko.anni.kit.AbsANNIKit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class CustomKit extends AbsANNIKit {

    private String inventory;
    private List<String> blacklist;

    public CustomKit(String id, String shortName,String icon, String name, List<String> lore, String inventory, List<String> blacklist) {
        super(id, shortName, name, icon, lore);

        this.inventory = inventory != null ? inventory : "";
        this.blacklist = blacklist;
    }

    public CustomKit(String id, String shortName, Material icon, String name, List<String> lore, ItemStack[] inventory, List<String> blacklist) {
        super(id, shortName, name, icon.name(), lore);

        setKitContents(inventory);
        this.blacklist = blacklist;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setIcon(String material) {
        Preconditions.checkArgument(Enums.getIfPresent(Material.class, material.toUpperCase()).isPresent());

        this.icon = material.toUpperCase();
    }

    public void setIcon(Material material) {
        this.icon = material.name();
    }

    @Override
    public Material getIcon() {
        return Enums.getIfPresent(Material.class, icon.toUpperCase()).or(Material.CHEST);
    }

    public String getRawIcon() {
        return icon.toUpperCase();
    }

    public void setLore(List<String> lore) {
        Preconditions.checkArgument(lore != null);

        this.lore = new ArrayList<>(lore);
    }

    public List<String> getLore() {
        return Collections.unmodifiableList(lore);
    }

    public void setKitContents(String contents) {
        Preconditions.checkArgument(contents != null);

        this.inventory = contents;
    }

    public void setKitContents(ItemStack[] array) {
        Preconditions.checkArgument(array != null);

        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             BukkitObjectOutputStream out = new BukkitObjectOutputStream(bytes)
        ) {
            out.writeObject(array);
            inventory = Base64.getEncoder().encodeToString(bytes.toByteArray());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ItemStack[] getKitContents() {
        byte[] ba = Base64.getDecoder().decode(inventory);
        try (ByteArrayInputStream bytes = new ByteArrayInputStream(ba);
             BukkitObjectInputStream in = new BukkitObjectInputStream(bytes)
        ) {
            return (ItemStack[]) in.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String getRawInventory() {
        return inventory;
    }

}