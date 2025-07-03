package de.t0bx.sentienceEntity.network.wrapper;

import io.netty.buffer.ByteBuf;

public interface PacketWrapper {
    ByteBuf build();
}
