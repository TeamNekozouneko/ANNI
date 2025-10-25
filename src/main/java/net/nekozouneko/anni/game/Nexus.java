package net.nekozouneko.anni.game;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Nexus {

    private int health;
    private int maxHealth;

    public Nexus(int health) {
        this.maxHealth = health;
        this.health = health;
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public void heal(int heal) {
        Preconditions.checkState(!isDestroyed());

        health = Math.min(health + heal, maxHealth);
    }

    public void damage(int damage) {
        Preconditions.checkState(!isDestroyed());

        health = Math.max(health - damage, 0);
    }

}
