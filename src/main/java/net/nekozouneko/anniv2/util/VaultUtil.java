package net.nekozouneko.anniv2.util;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.function.Consumer;

public final class VaultUtil {

    private VaultUtil() {
        throw new ExceptionInInitializerError();
    }

    public static Economy getEco() {
        RegisteredServiceProvider<Economy> prov =
                Bukkit.getServicesManager().getRegistration(Economy.class);

        return prov.getProvider();
    }

    public static boolean hasEco() {
        return Bukkit.getServicesManager().getRegistration(Economy.class) != null;
    }

    public static boolean ifAvail(Consumer<Economy> consumer) {
        if (hasEco()) {
            consumer.accept(getEco());
            return true;
        }

        return false;
    }

}
