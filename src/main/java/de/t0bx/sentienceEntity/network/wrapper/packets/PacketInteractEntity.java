/**
 SentienceEntity API License v1.1
 Copyright (c) 2025 (t0bx)

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to use, copy, modify, and integrate the Software into their own projects, including commercial and closed-source projects, subject to the following conditions:

 1. Attribution:
 You must give appropriate credit to the original author ("Tobias Schuster" or "t0bx"), provide a link to the source or official page if available, and indicate if changes were made. You must do so in a reasonable and visible manner, such as in your plugin.yml, README, or about page.

 2. No Redistribution or Resale:
 You may NOT sell, redistribute, or otherwise make the original Software or modified standalone versions of it available as a product (free or paid), plugin, or downloadable file, unless you have received prior written permission from the author. This includes publishing the plugin on any marketplace (e.g., SpigotMC, MC-Market, Polymart) or including it in paid bundles.

 3. Use as Dependency/API:
 You are allowed to use this Software as a dependency or library in your own plugin or project, including in paid products, as long as attribution is given and the Software itself is not being sold or published separately.

 4. No Misrepresentation:
 You may not misrepresent the origin of the Software. You must clearly distinguish your own modifications from the original work. The original author's name may not be removed from the source files or documentation.

 5. License Retention:
 This license notice and all conditions must be preserved in all copies or substantial portions of the Software.

 6. Disclaimer:
 THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY ARISING FROM THE USE OF THIS SOFTWARE.

 ---

 Summary (non-binding):
 You may use this plugin in your projects, even commercially, but you may not resell or republish it. Always give credit to t0bx.
 */

package de.t0bx.sentienceEntity.network.wrapper.packets;

import de.t0bx.sentienceEntity.network.interact.InteractHand;
import de.t0bx.sentienceEntity.network.interact.InteractType;
import de.t0bx.sentienceEntity.network.utils.PacketId;
import de.t0bx.sentienceEntity.network.utils.PacketUtils;
import de.t0bx.sentienceEntity.network.version.registries.PacketIdRegistry;
import de.t0bx.sentienceEntity.network.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;

import java.util.Optional;

@Getter
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class PacketInteractEntity implements PacketWrapper {

    private final int entityId;
    private final InteractType interactType;
    private final Optional<Float> targetX;
    private final Optional<Float> targetY;
    private final Optional<Float> targetZ;
    private final Optional<InteractHand> hand;
    private final boolean sneaking;
    private final int packetId = PacketIdRegistry.getPacketId(PacketId.INTERACT_ENTITY);

    /**
     * Constructs a new {@code PacketInteractEntity} instance representing a packet used for interacting
     * with entities in the game through various interaction types (e.g., attack, interact, or interact-at).
     * This packet includes optional target coordinates, the hand used for interaction, and whether the player
     * is sneaking during the action.
     *
     * @param entityId an integer representing the ID of the entity being interacted with.
     * @param interactType the type of interaction being performed, as defined in {@code InteractType}.
     * @param targetX an optional {@code Float} value representing the X-coordinate of the interaction target.
     * @param targetY an optional {@code Float} value representing the Y-coordinate of the interaction target.
     * @param targetZ an optional {@code Float} value representing the Z-coordinate of the interaction target.
     * @param hand an optional {@code InteractHand} value representing the hand used for the interaction.
     * @param sneaking a {@code boolean} indicating if the player is sneaking while performing the interaction.
     */
    public PacketInteractEntity(int entityId, InteractType interactType, Optional<Float> targetX, Optional<Float> targetY, Optional<Float> targetZ,
                                Optional<InteractHand> hand, boolean sneaking) {
        this.entityId = entityId;
        this.interactType = interactType;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.hand = hand;
        this.sneaking = sneaking;
    }

    /**
     * Reads data from the provided {@code ByteBuf} to construct a new {@code PacketInteractEntity} instance.
     * This method parses the entity ID, interaction type, optional target coordinates, the hand used for interaction,
     * and whether the player is sneaking.
     *
     * @param buf the {@code ByteBuf} from which the {@code PacketInteractEntity} data is read
     * @return the constructed {@code PacketInteractEntity} instance containing the parsed data
     */
    public static PacketInteractEntity read(ByteBuf buf) {
        int entityId = PacketUtils.readVarInt(buf);
        InteractType type = InteractType.from(PacketUtils.readVarInt(buf));

        Optional<Float> x = Optional.empty();
        Optional<Float> y = Optional.empty();
        Optional<Float> z = Optional.empty();
        Optional<InteractHand> hand = Optional.empty();

        switch (type) {
            case INTERACT_AT -> {
                x = Optional.of(buf.readFloat());
                y = Optional.of(buf.readFloat());
                z = Optional.of(buf.readFloat());
                hand = Optional.ofNullable(InteractHand.from(PacketUtils.readVarInt(buf)));
            }
            case INTERACT -> hand = Optional.ofNullable(InteractHand.from(PacketUtils.readVarInt(buf)));
            case ATTACK -> hand = Optional.of(InteractHand.NONE);
            case null -> {}
        }

        boolean sneaking = buf.readBoolean();
        return new PacketInteractEntity(entityId, type, x, y, z, hand, sneaking);
    }

    /**
     * Builds and serializes the current {@code PacketInteractEntity} instance into a {@code ByteBuf}.
     * This method writes the packet ID, entity ID, interaction type, optional target coordinates,
     * the hand used for the interaction, and whether the player is sneaking into the buffer.
     *
     * @return a {@code ByteBuf} containing the serialized data of the {@code PacketInteractEntity} instance
     */
    @Override
    public ByteBuf build() {
        ByteBuf buf = Unpooled.buffer();

        PacketUtils.writeVarInt(buf, packetId);

        PacketUtils.writeVarInt(buf, entityId);
        PacketUtils.writeVarInt(buf, interactType.getId());

        targetX.ifPresent(buf::writeFloat);
        targetY.ifPresent(buf::writeFloat);
        targetZ.ifPresent(buf::writeFloat);
        hand.ifPresent(h -> PacketUtils.writeVarInt(buf, h.getId()));

        PacketUtils.writeBoolean(buf, sneaking);

        return buf;
    }
}
