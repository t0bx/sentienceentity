# SentienceEntity

A powerful Minecraft plugin that adds interactive NPCs with advanced features to your Minecraft server.

## [License](https://github.com/t0bx/sentienceentity?tab=License-1-ov-file)

## Features

- Interactive NPCs with custom skins
- Hologram support for NPCs
- Packet-based entity handling for better performance
- Player interaction system
- Customizable NPC behaviors
- Skin fetching system for custom NPC appearances
- MiniMessage Format for Holograms

## Requirements

- **Spigot/Paper 1.21.X +**

## Commands

### NPC Management (`/se` or `/sentienceentity`)

| Command | Description | Permission |
|---------|-------------|------------|
| `/se spawnnpc <Name> <Player Name>` | Spawn a new NPC | `se.command` |
| `/se editnpc <Name> <option>` | Edit an NPC (see options below) | `se.command` |
| `/se removenpc <Name>` | Remove an NPC | `se.command` |
| `/se listnpc` | List all spawned NPCs | `se.command` |
| `/se inspect` | Enter inspector mode (left-click NPC for info, right-click hologram for info) | `se.command` |
| `/se cancel` | Cancel current NPC creation | `se.command` |

**Edit NPC Options:**
- `shouldLookAtPlayer` - Toggle if NPC looks at players
- `shouldSneakWithPlayer` - Toggle if player NPC sneaks with players
- `updateLocation` - Update NPC location to your current location
- `setSkin <Player Name>` - Set NPC skin (player NPCs only)
- `setItem <Slot>` - Set equipment item (Slots: mainhand, offhand, boots, leggings, chestplate, helmet)
- `removeItem <Slot>` - Remove equipment from NPC
- `setPermission <Permission>` - Set visibility permission (use "none" for no permission)
- `setPath <Path Name>` - Bind a path to the NPC

### Path Management (`/sp` or `/sentiencepath`)

| Command | Description | Permission |
|---------|-------------|------------|
| `/sp create <pathname> <Trigger-Type>` | Create a new path (Trigger types: LOOP, INTERACT) | `se.path` |
| `/sp remove <pathname>` | Remove a path | `se.path` |
| `/sp addPoint <pathname> <walk/teleport>` | Add a waypoint to a path | `se.path` |
| `/sp removePoint <pathname> <point index>` | Remove a waypoint from a path | `se.path` |
| `/sp listPoints <pathname>` | List all waypoints in a path | `se.path` |
| `/sp list` | List all created paths | `se.path` |
| `/sp apply <pathname> <npcname>` | Apply a path to an NPC | `se.path` |
| `/sp setTrigger <pathname> <Trigger Type>` | Change path trigger type | `se.path` |

### Hologram Management (`/sh` or `/sentiencehologram`)

| Command | Description | Permission |
|---------|-------------|------------|
| `/sh createHologram <NPC Name>` | Create a hologram for an NPC | `se.hologram` |
| `/sh addTextLine <NPC Name> <Text>` | Add a text line to hologram | `se.hologram` |
| `/sh addItemLine <NPC Name>` | Add an item line to hologram (hold item in hand) | `se.hologram` |
| `/sh setTextLine <NPC Name> <index> <Text>` | Update a text line | `se.hologram` |
| `/sh setItemLine <NPC Name> <index>` | Update an item line (hold item in hand) | `se.hologram` |
| `/sh lines <NPC Name>` | List all lines in a hologram | `se.hologram` |
| `/sh removeLine <NPC Name> <index>` | Remove a specific line from hologram | `se.hologram` |
| `/sh removeHologram <NPC Name>` | Remove entire hologram | `se.hologram` |

## Permissions

| Permission | Description |
|------------|-------------|
| `se.command` | Allows access to NPC management commands |
| `se.path` | Allows access to path management commands |
| `se.hologram` | Allows access to hologram management commands |

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
    <artifactId>sentienceentity</artifactId>
    <version>2.0.0</version>
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
        event.getNpc(); // The npc which the player interacts with
        event.getInteractType(); // Returns the Interact Type, ATTACK, INTERACT or INTERACT_AT

        //Example
        Player player = event.getPlayer();

        //Note if the InteractType is ATTACK the InteractHand is NONE
        //We only want that the player uses the main hand not the offhand
        if (event.getInteractHand() != InteractHand.MAIN_HAND) return;

        //We only want right clicks on the npc
        if (event.getInteractType() != InteractType.INTERACT) return;

        if (event.getNpc().getName().equalsIgnoreCase("test")) {
            player.sendMessage("You've right clicked the npc " + event.getNpcName());
        }
    }
}

```

If you encounter any problems or issues please contact me through discord: 84.tobi