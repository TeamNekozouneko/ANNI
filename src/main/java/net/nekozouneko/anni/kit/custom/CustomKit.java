package net.nekozouneko.anni.kit.custom;

import com.google.common.base.Enums;
import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.nekozouneko.anni.kit.Kit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class CustomKit implements Kit {

    private final String id;
    private String name, shortName;
    private String icon;

    private String inventory;

    public CustomKit(String id, String name, String shortName, String icon, String inventory) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.icon = icon;

        this.inventory = inventory != null ? inventory : "";
    }

    public CustomKit(String id, String shortName, Material icon, String name, ItemStack[] inventory) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.icon = icon.name();

        setKitContents(inventory);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Component getName(Locale locale) {
        return Component.text(name);
    }

    @Override
    public List<Component> getLore(Locale locale) {
        return Collections.emptyList();
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public Material getIcon() {
        return Enums.getIfPresent(Material.class, icon.toUpperCase()).or(Material.CHEST);
    }

    public void setIcon(String material) {
        Preconditions.checkArgument(Enums.getIfPresent(Material.class, material.toUpperCase()).isPresent());

        this.icon = material.toUpperCase();
    }

    public void setIcon(Material material) {
        this.icon = material.name();
    }

    public String getRawIcon() {
        return icon.toUpperCase();
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