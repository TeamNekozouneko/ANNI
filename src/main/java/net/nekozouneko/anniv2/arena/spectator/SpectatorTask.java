package net.nekozouneko.anniv2.arena.spectator;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.nekozouneko.anniv2.ANNIPlugin;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class SpectatorTask extends BukkitRunnable {

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (SpectatorManager.isSpectating(p)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 40, 255, false, false, true));
                p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 1, false, false, true));
                p.setAllowFlight(true);
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ANNIPlugin.getInstance().getMessageManager()
                        .build("actionbar.spectator_mode")
                ));

                SpectatorManager.getPlayers().stream()
                        .filter(uuid -> Bukkit.getPlayer(uuid) != null)
                        .map(Bukkit::getPlayer)
                        .forEach(p2 -> {
                            if (!p.equals(p2) && !SpectatorManager.isSpectating(p2)) {
                                p2.hidePlayer(ANNIPlugin.getInstance(), p);
                            }
                        });
            }
            else {
                Bukkit.getOnlinePlayers().forEach(p2 -> {
                    p2.showPlayer(ANNIPlugin.getInstance(), p);
                    if (p2.getGameMode() == GameMode.ADVENTURE) p2.setAllowFlight(false);
                });
            }
        }
    }

}
