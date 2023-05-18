package net.nekozouneko.anniv2.arena;

public enum ArenaState {

    WAITING(-2, null, null, null, 0, null),
    STARTING(-1, null, null, null, 60, null),

    PHASE_FIVE(5, "phase.name.5", "phase.description.5", 2, 0, null),
    PHASE_FOUR(4, "phase.name.4", "phase.description.4", 2, 600, PHASE_FIVE),
    PHASE_THREE(3, "phase.name.3", "phase.description.3", 1, 600, PHASE_FOUR),
    PHASE_TWO(2, "phase.name.2", "phase.description.2", 1, 600, PHASE_THREE),
    PHASE_ONE(1, "phase.name.1", "phase.description.1", null, 600, PHASE_TWO),

    GAME_OVER(0, "bossbar.timer.state.restarting", null, null, 30, null),

    STOPPED(-3, null, null, null, 0, null);

    private final int id;
    private final String name;
    private final String description;

    private final Integer nexusDamage;
    private final long nextPhaseIn;
    private final ArenaState nextPhase;

    private ArenaState(int id, String name, String desc, Integer nexusDamage, long nextPhaseIn, ArenaState nextPhase) {
        this.id = id;
        this.name = name;
        this.nexusDamage = nexusDamage;
        this.nextPhaseIn = nextPhaseIn;
        this.nextPhase = nextPhase;
        this.description = desc;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
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
