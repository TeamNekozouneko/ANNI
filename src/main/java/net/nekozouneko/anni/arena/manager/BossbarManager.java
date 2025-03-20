package net.nekozouneko.anni.arena.manager;

import net.nekozouneko.anni.ANNIConfig;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.ANNIArena;
import net.nekozouneko.anni.arena.team.ANNITeam;
import net.nekozouneko.anni.message.MessageManager;
import net.nekozouneko.anni.util.CmnUtil;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BossbarManager {

    private final ANNIArena arena;
    private final BossBar bossBar;

    public BossbarManager(ANNIArena arena, BossBar bossBar) {
        this.arena = arena;
        this.bossBar = bossBar;
    }
    
    public void update() {
        Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);

        MessageManager mm = ANNIPlugin.getInstance().getMessageManager();

        bossBar.setColor(BarColor.BLUE);
        switch (arena.getState()) {
            case PHASE_ONE:
            case PHASE_TWO:
            case PHASE_THREE:
            case PHASE_FOUR:
            case GAME_OVER: {
                bossBar.setVisible(true);
                bossBar.setTitle(
                        mm.build("bossbar.timer",
                                mm.build(arena.getState().getName()),
                                CmnUtil.secminTimer(arena.getTimer())
                        )
                );
                bossBar.setProgress(arena.getState().nextPhaseIn() > 0 ? CmnUtil.bossBarProgress(arena.getState().nextPhaseIn(), arena.getTimer()) : 1);
                break;
            }
            case PHASE_FIVE: {
                bossBar.setVisible(true);
                bossBar.setTitle(mm.build(arena.getState().getName()));
                bossBar.setProgress(1);
                break;
            }
            default: {
                bossBar.setVisible(false);
                break;
            }
        }
    }

    public void damageNexus(ANNITeam target, Player damager, int health) {
        bossBar.setProgress((double) health / ANNIConfig.getDefaultHealth());

        if (bossBar.getProgress() <= 0.2) bossBar.setColor(BarColor.RED);
        else if (bossBar.getProgress() <= 0.5) bossBar.setColor(BarColor.YELLOW);
        else bossBar.setColor(BarColor.GREEN);

        MessageManager mm = ANNIPlugin.getInstance().getMessageManager();

        bossBar.setTitle(mm.build("bossbar.damaged_nexus",
                damager.getName(), arena.getTeam(target).getName()
        ));
    }
}
