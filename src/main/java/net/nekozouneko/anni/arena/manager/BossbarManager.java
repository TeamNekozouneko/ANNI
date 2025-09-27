package net.nekozouneko.anni.arena.manager;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.nekozouneko.anni.ANNIConfig;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.ANNIArena;
import net.nekozouneko.anni.arena.team.ANNITeam;
import net.nekozouneko.anni.message.TranslationManager;
import net.nekozouneko.anni.util.CmnUtil;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;

import java.util.*;

public class BossbarManager {

    private final ANNIArena arena;
    private final Map<Locale, KeyedBossBar> bossBars = new HashMap<>();

    public BossbarManager(ANNIArena arena) {
        this.arena = arena;
    }

    public void leave(Player player) {
        bossBars.values().forEach(bossBar -> bossBar.removePlayer(player));
    }
    
    public void update() {
        Set<Locale> locales = new HashSet<>();

        Bukkit.getOnlinePlayers().forEach(player -> {
            locales.add(player.locale());
            var bossBar = bossBars.get(player.locale());

            if (bossBar == null) {
                bossBar = Bukkit.createBossBar(new NamespacedKey(ANNIPlugin.getInstance(), player.locale().toLanguageTag().toLowerCase()), "", BarColor.BLUE, BarStyle.SOLID);
                bossBars.put(player.locale(), bossBar);
            }

            bossBar.addPlayer(player);
        });

        if (!locales.containsAll(bossBars.keySet())) {
            new HashSet<>(bossBars.keySet()).forEach(locale -> {
                if (!locales.contains(locale)) Bukkit.removeBossBar(bossBars.remove(locale).getKey());
            });
        }

        TranslationManager translation = ANNIPlugin.getInstance().getTranslationManager();

        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();

        bossBars.forEach((locale, bossBar) -> {
            bossBar.setColor(BarColor.BLUE);
            switch (arena.getState()) {
                case PHASE_ONE:
                case PHASE_TWO:
                case PHASE_THREE:
                case PHASE_FOUR:
                case GAME_OVER: {
                    bossBar.setVisible(true);
                    bossBar.setTitle(serializer.serialize(
                            translation.component(locale, "bossbar.timer",
                                    translation.component(locale, arena.getState().getName()),
                                    CmnUtil.secminTimer(arena.getTimer())
                            )
                    ));
                    bossBar.setProgress(arena.getState().nextPhaseIn() > 0 ? CmnUtil.bossBarProgress(arena.getState().nextPhaseIn(), arena.getTimer()) : 1);
                    break;
                }
                case PHASE_FIVE: {
                    bossBar.setVisible(true);
                    bossBar.setTitle(serializer.serialize(translation.component(locale, arena.getState().getName())));
                    bossBar.setProgress(1);
                    break;
                }
                default: {
                    bossBar.setVisible(false);
                    break;
                }
            }
        });
    }

    public void damageNexus(ANNITeam target, Player damager, int health) {
        double progress = (double) health / ANNIConfig.getDefaultHealth();

        BarColor color;
        if (progress <= 0.2) color = BarColor.RED;
        else if (progress <= 0.5) color = BarColor.YELLOW;
        else color = BarColor.GREEN;

        TranslationManager translation = ANNIPlugin.getInstance().getTranslationManager();
        var serializer = LegacyComponentSerializer.legacySection();

        bossBars.forEach((locale, bossBar) -> {
            bossBar.setColor(color);
            bossBar.setProgress(progress);
            bossBar.setTitle(serializer.serialize(translation.component(locale, "bossbar.damaged_nexus",
                    damager.name(), translation.component(locale, target.getNameKey())
            )));
        });
    }

    public void delete() {
        bossBars.forEach((locale, bossBar) -> Bukkit.removeBossBar(bossBar.getKey()));
    }
}
