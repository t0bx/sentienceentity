/**
 * SentienceEntity API License v1.1
 * Copyright (c) 2025 (t0bx)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to use, copy, modify, and integrate the Software into their own projects, including commercial and closed-source projects, subject to the following conditions:
 * <p>
 * 1. Attribution:
 * You must give appropriate credit to the original author ("Tobias Schuster" or "t0bx"), provide a link to the source or official page if available, and indicate if changes were made. You must do so in a reasonable and visible manner, such as in your plugin.yml, README, or about page.
 * <p>
 * 2. No Redistribution or Resale:
 * You may NOT sell, redistribute, or otherwise make the original Software or modified standalone versions of it available as a product (free or paid), plugin, or downloadable file, unless you have received prior written permission from the author. This includes publishing the plugin on any marketplace (e.g., SpigotMC, MC-Market, Polymart) or including it in paid bundles.
 * <p>
 * 3. Use as Dependency/API:
 * You are allowed to use this Software as a dependency or library in your own plugin or project, including in paid products, as long as attribution is given and the Software itself is not being sold or published separately.
 * <p>
 * 4. No Misrepresentation:
 * You may not misrepresent the origin of the Software. You must clearly distinguish your own modifications from the original work. The original author's name may not be removed from the source files or documentation.
 * <p>
 * 5. License Retention:
 * This license notice and all conditions must be preserved in all copies or substantial portions of the Software.
 * <p>
 * 6. Disclaimer:
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY ARISING FROM THE USE OF THIS SOFTWARE.
 * <p>
 * ---
 * <p>
 * Summary (non-binding):
 * You may use this plugin in your projects, even commercially, but you may not resell or republish it. Always give credit to t0bx.
 */

package de.t0bx.sentienceEntity.network.version;

import de.t0bx.sentienceEntity.SentienceEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@RequiredArgsConstructor
public enum ProtocolVersion {
    V1_21_4("1.21.4", 769),
    V1_21_5("1.21.5", 770),
    V1_21_6("1.21.6", 771),
    V1_21_7("1.21.7", 772);

    private final String versionString;
    private final int protocolId;

    /**
     * Detects the current protocol version of the server by matching the Minecraft server version
     * string with the version strings defined in the enum constants.
     *
     * @return the corresponding {@link ProtocolVersion} based on the server's Minecraft version
     * @throws IllegalStateException if the server version does not match any known protocol version
     */
    public static ProtocolVersion detect() {
        String version = Bukkit.getVersion();
        Pattern pattern = Pattern.compile("\\(MC: ([^)]+)\\)");
        Matcher matcher = pattern.matcher(version);

        if (matcher.find()) {
            String serverVersion = matcher.group(1);
            for (ProtocolVersion protocolVersion : values()) {
                if (serverVersion.contains(protocolVersion.versionString)) return protocolVersion;
            }

            Bukkit.getPluginManager().disablePlugin(SentienceEntity.getInstance());
            throw new IllegalStateException("SentienceEntity doesn't support Version: " + serverVersion);
        }

        return null;
    }
}
