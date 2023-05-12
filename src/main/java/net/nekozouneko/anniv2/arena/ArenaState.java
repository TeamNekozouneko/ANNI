package net.nekozouneko.anniv2.arena;

public enum ArenaState {

    WAITING(-2, null, false, 0, null),
    STARTING(-1, null, false, 60, null),

    PHASE_FIVE(5, "bossbar.timer.state.5", true, 0, null),
    PHASE_FOUR(4, "bossbar.timer.state.4", true, 600, PHASE_FIVE),
    PHASE_THREE(3, "bossbar.timer.state.3", true, 600, PHASE_FOUR),
    PHASE_TWO(2, "bossbar.timer.state.2", true, 600, PHASE_THREE),
    PHASE_ONE(1, "bossbar.timer.state.1", false, 600, PHASE_TWO),

    GAME_OVER(0, "bossbar.timer.state.restarting", false, 30, null),

    STOPPED(-3, null, false, 0, null);

    private final int id;
    private final String timerStateKey;
    private final boolean canDestroyNexus;
    private final long nextPhaseIn;
    private final ArenaState nextPhase;

    private ArenaState(int id, String timerStateKey, boolean canDestroyNexus, long nextPhaseIn, ArenaState nextPhase) {
        this.id = id;
        this.timerStateKey = timerStateKey;
        this.canDestroyNexus = canDestroyNexus;
        this.nextPhaseIn = nextPhaseIn;
        this.nextPhase = nextPhase;
    }

    public int getId() {
        return id;
    }

    public String getTimerStateKey() {
        return timerStateKey;
    }

    public boolean canDestroyNexus() {
        return canDestroyNexus;
    }

    public long nextPhaseIn() {
        return nextPhaseIn;
    }

    public ArenaState nextPhase() {
        return nextPhase;
    }

}
