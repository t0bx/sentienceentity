package de.t0bx.sentienceEntity.packet.utils;

import de.t0bx.sentienceEntity.packet.wrapper.packets.PacketPlayerInfoUpdate;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class NpcProfile {
    private final int entityId;
    private final UUID uuid;
    private final List<PacketPlayerInfoUpdate.Property> properties;
}
