package de.t0bx.sentienceEntity.network.utils;

import lombok.Getter;

@Getter
public enum TeamMethods {
    CREATE_TEAM(0);

    private final int id;

    TeamMethods(int id) {
        this.id = id;
    }
}
