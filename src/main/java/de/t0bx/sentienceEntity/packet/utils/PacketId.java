package de.t0bx.sentienceEntity.packet.utils;

import lombok.Getter;

@Getter
public enum PacketId {
    CHAT(0x63),
    PLAYER_INFO_UPDATE(0x40),
    PLAYER_INFO_REMOVE(0x3F),
    SPAWN_ENTITY(0x01),
    SET_ENTITY_METADATA(0x5D),
    SET_HEAD_ROTATION(0x4D),
    UPDATE_ENTITY_ROTATION(0x32),
    REMOVE_ENTITY(0x47),
    TELEPORT_ENTITY(0x20);

    public final int id;

    PacketId(int id) {
        this.id = id;
    }
}
