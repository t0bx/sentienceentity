# SentienceEntity

A powerful SpigotMC plugin that adds interactive NPCs with advanced features to your Minecraft server.

## Features

- Interactive NPCs with custom skins
- Hologram support for NPCs
- Packet-based entity handling for better performance
- Player interaction system
- Customizable NPC behaviors
- Skin fetching system for custom NPC appearances
- MiniMessage Format for Holograms

## Requirements

- Spigot/Paper 1.21
- PacketEvents 2.8.0

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

## Dependencies

- PacketEvents (2.8.0) (Required)

## Installation

1. Download the latest version of SentienceEntity
2. Place the jar file in your server's `plugins` folder
3. Install PacketEvents plugin
4. Restart your server

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
    <version>1.2</version>
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

Working with the PlayerClickNPCEvent

```java
import de.t0bx.sentienceEntity.events.PlayerClickNPCEvent;
import de.t0bx.sentienceEntity.utils.ClickType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NPCClickListener implements Listener {

    @EventHandler
    public void onNPCClick(PlayerClickNPCEvent event) {
        event.getClickType(); // Returns if its RIGHT_CLICK or LEFT_CLICK
        event.getPlayer(); // The Player who interacts with the entity
        event.getNpcName(); // The npcName which the player interacts with

        //Example
        if (event.getClickType() == ClickType.RIGHT_CLICK) {
            if (event.getNpcName().equalsIgnoreCase("test")) {
                event.getPlayer().sendMessage("You've right clicked the npc with the name test!");
            }
        }
        
        if (event.getClickType() == ClickType.LEFT_CLICK) {
            if (event.getNpcName().equalsIgnoreCase("test2")) {
                event.getPlayer().sendMessage("You've left clicked the npc with the name test2!");
            }
        }
    }
}

```

If you encounter any problems or issues please contact me through discord: 84.tobi
