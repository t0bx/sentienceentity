package de.t0bx.sentienceEntity.packet.wrapper;

import io.netty.buffer.ByteBuf;

public interface PacketWrapper {
    ByteBuf build();
}
