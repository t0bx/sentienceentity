package de.t0bx.sentienceEntity.network.version;

public class VersionRegistry {
    private static final ProtocolVersion VERSION = ProtocolVersion.detect();

    public static ProtocolVersion getVersion() {
        return VERSION;
    }
}
