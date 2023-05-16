package net.nekozouneko.anniv2.arena;

public enum ArenaState {

    WAITING(-2, null, null, 0, null),
    STARTING(-1, null, null, 60, null),

    PHASE_FIVE(5, "bossbar.timer.state.5", 2, 0, null),
    PHASE_FOUR(4, "bossbar.timer.state.4", 2, 600, PHASE_FIVE),
    PHASE_THREE(3, "bossbar.timer.state.3", 1, 600, PHASE_FOUR),
    PHASE_TWO(2, "bossbar.timer.state.2", 1, 600, PHASE_THREE),
    PHASE_ONE(1, "bossbar.timer.state.1", null, 600, PHASE_TWO),

    GAME_OVER(0, "bossbar.timer.state.restarting", null, 30, null),

    STOPPED(-3, null, null, 0, null);

    private final int id;
    private final String timerStateKey;
    private final Integer nexusDamage;
    private final long nextPhaseIn;
    private final ArenaState nextPhase;

    private ArenaState(int id, String timerStateKey, Integer nexusDamage, long nextPhaseIn, ArenaState nextPhase) {
        this.id = id;
        this.timerStateKey = timerStateKey;
        this.nexusDamage = nexusDamage;
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
        return nexusDamage != null;
    }

    public int getNexusDamage() {
        return nexusDamage != null ? nexusDamage : 0;
    }

    public long nextPhaseIn() {
        return nextPhaseIn;
    }

    public ArenaState nextPhase() {
        return nextPhase;
    }

}
