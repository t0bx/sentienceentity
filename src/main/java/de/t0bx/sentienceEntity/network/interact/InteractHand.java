package de.t0bx.sentienceEntity.network.interact;

import lombok.Getter;

@Getter
public enum InteractHand {
    MAIN_HAND(0),
    OFF_HAND(1),
    NONE(-1);

    private final int id;

    InteractHand(int id) {
        this.id = id;
    }

    public static InteractHand from(int id) {
        for (InteractHand t : values()) {
            if (t.id == id)
                return t;
        }
        return null;
    }
}
