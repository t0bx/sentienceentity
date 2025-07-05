# SentienceEntity

A powerful Minecraft plugin that adds interactive NPCs with advanced features to your Minecraft server.

## License

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

## Features

- Interactive NPCs with custom skins
- Hologram support for NPCs
- Packet-based entity handling for better performance
- Player interaction system
- Customizable NPC behaviors
- Skin fetching system for custom NPC appearances
- MiniMessage Format for Holograms

## Requirements

- **Spigot/Paper 1.21.4 - 1.21.7 +**

## Commands

| Command                             | Description | Permission |
|-------------------------------------|-------------|------------|
| `/se`                               | Main command for SentienceEntity | `se.command` |
| `/se spawnnpc <Name> <Player Name>` | Spawn a new npc | `se.command` |
| `/se editnpc <Name>`                | Edit a npc | `se.command` |
| `/se removenpc <Name>`              | Removes a npc | `se.command` |
| `/se listnpc`                       | List all npcs | `se.command` |
| `/se createHologram <Name>`         | Create a Hologram for a npc | `se.command` |
| `/se addLine <Name> <Text>`         | Add a line for a hologram | `se.command` |
| `/se setLine <Name> <index> <Text>` | Updates a specific line from a hologram | `se.command` |
| `/se lines <Name>`                  | List all lines from a hologram | `se.command` |
| `/se removeLine <Name> <index>`     | Removes a specific line from a hologram | `se.command` |
| `/se removeHologram <Name>`         | Removes a hologram | `se.command` |

## Permissions

| Permission | Description |
|------------|-------------|
| `se.command` | Allows access to the main plugin command |

## Installation

1. Download the latest version of SentienceEntity
2. Place the jar file in your paper server's `plugins` folder
3. Restart your server

## Development
It's also possible to use SentienceEntity as a API for developing.

This Project uses Maven:
```xml
<repository>
    <id>spigotmc-releases</id>
    <url>https://repository.t0bx.de/repository/spigotmc-releases/</url>
</repository>
```

```xml
<dependency>
    <groupId>de.t0bx</groupId>
    <artifactId>SentienceEntity</artifactId>
    <version>1.7</version>
    <scope>provided</scope>
</dependency>
```

## How to work with:

Working with npcs

```java
import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.hologram.SentienceHologram;
import de.t0bx.sentienceEntity.npc.SentienceNPC;

public void npcExamples() {
    //Creates a npc, name must be unique
    SentienceEntity.getApi().getNpcsHandler().createNPC(npcName, playerName, location);
    
    SentienceEntity.getApi().getNpcsHandler().createNPC(npcName, playerName, location, () -> {
        //Callback when npc got created
    });

    //Creating npc without fetching the skinValue and skinSignature from the skinfetcher
    SentienceEntity.getApi().getNpcsHandler().createNPC(npcName, location, skinValue, skinSiganture);

    SentienceNPC npc = SentienceEntity.getApi().getNpcsHandler().getNPC(npcName); //Returns the npc class

    //There are even more methods you can work with
}

public void hologramExamples() {
    SentienceEntity.getApi().getHologramManager().createHologram(npcName, location); //Creates a hologram based on the npcName

    SentienceHologram hologram = SentienceEntity.getApi().getHologramManager().getHologram(npcName); //Returns the hologram class
    
    //Also there are even more methods you can work with
}
```

Setting in api-only mode

```java
import de.t0bx.sentienceEntity.SentienceEntity;

public void example() {
    SentienceEntity.getApi().setApiOnly(true); //-> When activated the /se command doesn't work anymore
}
```

Working with the PlayerClickNPCEvent

```java
import de.t0bx.sentienceEntity.events.PlayerClickNpcEvent;
import de.t0bx.sentienceEntity.network.interact.InteractHand;
import de.t0bx.sentienceEntity.network.interact.InteractType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NpcClickListener implements Listener {

    @EventHandler
    public void onPlayerClickNpc(PlayerClickNpcEvent event) {
        event.getInteractHand(); // Returns the Interacted Hand, MAIN_HAND, OFF_HAND or NONE
        event.getPlayer(); // The Player who interacts with the entity
        event.getNpcName(); // The npcName which the player interacts with
        event.getInteractType(); // Returns the Interact Type, ATTACK, INTERACT or INTERACT_AT

        //Example
        Player player = event.getPlayer();

        //Note if the InteractType is ATTACK the InteractHand is NONE
        //We only want that the player uses the main hand not the offhand
        if (event.getInteractHand() != InteractHand.MAIN_HAND) return;

        //We only want right clicks on the npc
        if (event.getInteractType() != InteractType.INTERACT) return;

        if (event.getNpcName().equalsIgnoreCase("test")) {
            player.sendMessage("You've right clicked the npc " + event.getNpcName());
        }
    }
}

```

If you encounter any problems or issues please contact me through discord: 84.tobi
