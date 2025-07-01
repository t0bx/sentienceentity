package de.t0bx.sentienceEntity.packet.utils;

import lombok.Getter;

@Getter
public enum EntityType {
    ARMOR_STAND(5),
    PLAYER(147);

    public final int id;

    EntityType(int id) {
        this.id = id;
    }
}
