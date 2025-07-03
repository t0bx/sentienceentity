package de.t0bx.sentienceEntity.network.version;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

@Getter
@RequiredArgsConstructor
public enum ProtocolVersion {
    V1_21_4("1.21.4", 	769),
    V1_21_5("1.21.5", 770);

    private final String versionString;
    private final int protocolId;

    public static ProtocolVersion detect() {
        String serverVersion = Bukkit.getServer().getMinecraftVersion();
        for (ProtocolVersion version : values()) {
            if (serverVersion.contains(version.versionString)) return version;
        }
        throw new IllegalStateException("SentienceEntity doesn't support Version: " + serverVersion);
    }
}
