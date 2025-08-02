package de.t0bx.sentienceEntity.network.wrapper.packets;

import de.t0bx.sentienceEntity.network.inventory.equipment.Equipment;
import de.t0bx.sentienceEntity.network.utils.PacketId;
import de.t0bx.sentienceEntity.network.utils.PacketUtils;
import de.t0bx.sentienceEntity.network.version.registries.PacketIdRegistry;
import de.t0bx.sentienceEntity.network.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.List;

public class PacketSetEquipment implements PacketWrapper {

    private final int entityId;
    private final List<Equipment> equipment;
    private final int packetId = PacketIdRegistry.getPacketId(PacketId.SET_EQUIPMENT);

    public PacketSetEquipment(int entityId, List<Equipment> equipment) {
        this.entityId = entityId;
        this.equipment = equipment;
    }

    @Override
    public ByteBuf build() {
        ByteBuf buf = Unpooled.buffer();

        PacketUtils.writeVarInt(buf, packetId);
        PacketUtils.writeVarInt(buf, entityId);

        for (int i = 0; i < equipment.size(); i++) {
            Equipment eq = equipment.get(i);

            int slotId = eq.getSlot().getId();
            boolean isLast = (i == equipment.size() - 1);

            if (!isLast) {
                slotId |= 0x80;
            }

            buf.writeByte(slotId);
            PacketUtils.writeItemStack(buf, eq.getItemStack());
        }

        return buf;
    }
}
