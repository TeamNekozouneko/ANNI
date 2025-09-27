package net.nekozouneko.anni.arena;

import lombok.Getter;

public enum ArenaState {

    WAITING(-2, null, null, null, 0, null),
    STARTING(-1, null, null, null, 60, null),

    PHASE_FIVE(5, "phase.five.name", "phase.five.summary", 2, 0, null),
    PHASE_FOUR(4, "phase.four.name", "phase.four.summary", 2, 600, PHASE_FIVE),
    PHASE_THREE(3, "phase.three.name", "phase.three.summary", 1, 600, PHASE_FOUR),
    PHASE_TWO(2, "phase.two.name", "phase.two.summary", 1, 600, PHASE_THREE),
    PHASE_ONE(1, "phase.one.name", "phase.one.summary", null, 600, PHASE_TWO),

    GAME_OVER(0, "phase.restarting.name", null, null, 30, null),

    STOPPED(-3, null, null, null, 0, null);

    @Getter
    private final int id;
    @Getter
    private final String name;
    @Getter
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

    public boolean isInArena() {
        return id >= 0;
    }

}
