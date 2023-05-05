package net.nekozouneko.anniv2.arena;

public enum ArenaState {

    WAITING(-2, null),
    STARTING(-1, null),

    PHASE_ONE(1, "bossbar.timer.state.1"),
    PHASE_TWO(2, "bossbar.timer.state.2"),
    PHASE_THREE(3, "bossbar.timer.state.3"),
    PHASE_FOUR(4, "bossbar.timer.state.4"),
    PHASE_FIVE(5, "bossbar.timer.state.5"),

    GAME_OVER(0, "bossbar.timer.state.restarting"),

    STOPPED(-3, null);

    private final int id;
    private final String timerStateKey;

    private ArenaState(int id, String timerStateKey) {
        this.id = id;
        this.timerStateKey = timerStateKey;
    }

    public int getId() {
        return id;
    }

    public String getTimerStateKey() {
        return timerStateKey;
    }

}
