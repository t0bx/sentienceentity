package de.t0bx.sentienceEntity.network.interact;

import lombok.Getter;

@Getter
public enum InteractType {
    INTERACT(0),
    ATTACK(1),
    INTERACT_AT(2);

    private final int id;

    InteractType(int id) {
        this.id = id;
    }

    public static InteractType from(int id) {
        for (InteractType t : values()) {
            if (t.id == id)
                return t;
        }
        return null;
    }
}
