package de.t0bx.sentienceEntity.path;

import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.network.wrapper.PacketWrapper;
import de.t0bx.sentienceEntity.network.wrapper.packets.PacketSetHeadRotation;
import de.t0bx.sentienceEntity.network.wrapper.packets.PacketTeleportEntity;
import de.t0bx.sentienceEntity.npc.SentienceNPC;
import de.t0bx.sentienceEntity.path.data.Node;
import de.t0bx.sentienceEntity.path.data.SentiencePath;
import de.t0bx.sentienceEntity.path.data.SentiencePointPath;
import de.t0bx.sentienceEntity.path.serializer.PathSerializer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Represents an executor responsible for handling the movement and path traversal
 * of an entity within a virtual world environment. The executor is designed to
 * manage the navigation process, including pathfinding, interpolation, movement updates,
 * and synchronization with client-side visual representations.
 *
 * The {@code SentiencePathExecutor} operates using a defined path ({@code SentiencePath})
 * associated with a specific entity, identified by the entity's unique ID. It provides
 * functionalities to prepare, execute, and manage paths that an entity traverses, ensuring
 * smooth and natural movement across the environment.
 */
public record SentiencePathExecutor(int entityId, SentiencePath path) {

    /**
     * Prepares and initializes the execution of a movement path for an entity asynchronously.
     * This method attempts to load a pre-saved path for the entity. If no saved path is found,
     * it ensures the path is generated and interpolated, then saves the generated path for
     * future use. Once the path is successfully loaded or generated, it starts the navigation.
     *
     * Asynchronous processing is used to avoid blocking the main thread while performing
     * potentially time-consuming operations such as file I/O or path interpolation. Any
     * exceptions or issues encountered during the path loading or saving process are logged.
     *
     * If the path is successfully loaded, the NPC's movement is initiated. Otherwise, warnings
     * are logged to indicate that no valid path was found.
     */
    public void preparePath() {
        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        List<Location> locationsPath = PathSerializer.loadPath(path.getName());
                        if (locationsPath == null) {
                            locationsPath = ensurePathLoaded(path);

                            if (locationsPath != null) {
                                PathSerializer.savePath(path.getName(), locationsPath);
                            }
                        }
                        return locationsPath;
                    } catch (IOException exception) {
                        SentienceEntity.getInstance().getLogger().log(Level.WARNING, "Error loading/saving path " + path.getName(), exception);
                        return null;
                    }
                })
                .thenAccept(locationsPath -> {
                    if (locationsPath != null) {
                        startPath(locationsPath);
                    } else {
                        SentienceEntity.getInstance().getLogger().warning("Loaded path was null for " + path.getName());
                    }
                })
                .exceptionally(ex -> {
                    SentienceEntity.getInstance().getLogger().log(Level.SEVERE, "Failed to load path asynchronously: " + path.getName(), ex);
                    return null;
                });
    }

    /**
     * Starts the execution of a movement path for an NPC by iterating through a given sequence of locations.
     * The NPC moves from one location to the next, with each step involving orientation adjustments (yaw and pitch)
     * to simulate natural movement. This process is managed by a repeating task.
     *
     * @param paths a list of {@code Location} objects representing the waypoints the NPC should traverse
     */
    private void startPath(List<Location> paths) {
        String npcName = SentienceEntity.getInstance().getNpcshandler().getNpcNameFromId(entityId);
        SentienceNPC npc = SentienceEntity.getInstance().getNpcshandler().getNPC(npcName);

        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (index >= paths.size() - 1) {
                    cancel();
                    for (var players : npc.getChannels()) {
                        players.sendMultiplePackets(
                                sendMovementPacket(npc, npc.getLocation(), npc.getLocation().getYaw(), npc.getLocation().getPitch()),
                                sendHeadRotationPacket(npc, npc.getLocation().getYaw())
                        );
                    }
                    return;
                }

                Location current = paths.get(index);
                Location next = paths.get(index + 1);

                float yaw = calculateYaw(current, next);
                float pitch = calculatePitch(current, next);

                if (next.getY() > current.getY()) {
                    Location jumpLoc = next.clone();
                    jumpLoc.setY(jumpLoc.getY() + 0.2);
                    jumpLoc.setYaw(yaw);
                    jumpLoc.setPitch(pitch);

                    for (var players : npc.getChannels()) {
                        players.sendMultiplePackets(
                                sendMovementPacket(npc, jumpLoc, yaw, pitch),
                                sendHeadRotationPacket(npc, yaw)
                        );
                    }

                    index++;
                    return;
                }

                for (var players : npc.getChannels()) {
                    players.sendMultiplePackets(
                            sendMovementPacket(npc, next, yaw, pitch),
                            sendHeadRotationPacket(npc, yaw)
                    );
                }
                index++;
            }
        }.runTaskTimer(SentienceEntity.getInstance(), 0L, 2L);
    }

    /**
     * Sends a movement packet to update the position and orientation of the specified NPC
     * to the given location and directions (yaw and pitch). This ensures the NPC's movement
     * and rotation are synchronized with the client.
     *
     * @param npc      the {@code SentienceNPC} whose movement is being updated
     * @param location the {@code Location} representing the new position of the NPC
     * @param yaw      the yaw angle (horizontal rotation) of the NPC
     * @param pitch    the pitch angle (vertical rotation) of the NPC
     */
    private PacketWrapper sendMovementPacket(SentienceNPC npc, Location location, float yaw, float pitch) {
        location.setYaw(yaw);
        location.setPitch(pitch);
        return new PacketTeleportEntity(npc.getEntityId(), location, 0, 0, 0, true);
    }

    /**
     * Sends a packet to update the head rotation of the specified NPC to a given yaw angle.
     * This method creates a head rotation packet and broadcasts it to all players
     * currently connected to the NPC's channels.
     *
     * @param npc the {@code SentienceNPC} whose head rotation is being updated
     * @param yaw the yaw angle (horizontal rotation) to set for the NPC's head
     */
    private PacketWrapper sendHeadRotationPacket(SentienceNPC npc, float yaw) {
        return new PacketSetHeadRotation(npc.getEntityId(), yaw);
    }

    /**
     * Calculates the yaw angle between two locations in a 2D plane.
     * The yaw is the angle of rotation around the Y-axis and represents
     * the horizontal direction of movement from the starting location
     * to the target location.
     *
     * @param from the starting location
     * @param to   the target location
     * @return the yaw angle in degrees, measured clockwise from the positive Z-axis,
     * in the range of [0, 360).
     */
    private float calculateYaw(Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();

        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        return yaw < 0 ? yaw + 360 : yaw;
    }

    /**
     * Calculates the pitch angle between two locations in 3D space.
     * The pitch is the angle of rotation around the X-axis and represents
     * the upward or downward direction of the movement between the two locations.
     *
     * @param from the starting location
     * @param to   the target location
     * @return the pitch angle in degrees, negative for downward direction and positive for upward direction
     */
    private float calculatePitch(Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double dz = to.getZ() - from.getZ();

        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        double pitch = -Math.toDegrees(Math.atan2(dy, horizontalDistance));

        return (float) (pitch * 0.5);
    }

    /**
     * Ensures that all segments of the given {@code SentiencePath} are loaded and fully interpolated.
     * This method reconstructs the path by interpolating points between adjacent locations in the path,
     * ensuring a smooth traversal for the entity.
     *
     * @param path the {@code SentiencePath} object containing the sequence of points to be processed
     * @return a list of {@code Location} objects representing the fully loaded and interpolated path
     */
    public List<Location> ensurePathLoaded(SentiencePath path) {
        Map<Integer, SentiencePointPath> paths = path.getPaths();

        List<Integer> sortedKeys = new ArrayList<>(paths.keySet());
        Collections.sort(sortedKeys);

        List<Location> allPoints = new ArrayList<>();

        for (int i = 0; i < sortedKeys.size() - 1; i++) {
            int fromIndex = sortedKeys.get(i);
            int toIndex = sortedKeys.get(i + 1);

            SentiencePointPath fromPath = paths.get(fromIndex);
            SentiencePointPath toPath = paths.get(toIndex);

            Location loc = toPath.getLocation();
            World world = loc.getWorld();
            Block below = world.getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());

            if (!toPath.isTeleport() && below.isPassable()) {
                SentienceEntity.getInstance().getLogger().severe(
                        "Error while trying to calculate Path '" + path.getName() + "'  on index " + toIndex + " (" + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() +
                                ") is not reachable with walking! " +
                                "Please make sure that it's possible to walk! " +
                                "Max. 1 block high, Max. 1 block deep and no jumps! " +
                                "Otherwise use teleport!"
                );
                return null;
            }

            if (toPath.isTeleport()) {
                if (!allPoints.isEmpty()) {
                    Location last = allPoints.get(allPoints.size() - 1);
                    if (!last.equals(toPath.getLocation())) {
                        allPoints.add(toPath.getLocation());
                    }
                } else {
                    allPoints.add(toPath.getLocation());
                }
                continue;
            }

            List<Location> segmentPoints = findSimplePath(
                    fromPath.getLocation(),
                    toPath.getLocation(),
                    fromPath.getLocation().getWorld()
            );

            List<Location> interpolatedSegment = new ArrayList<>();
            for (int j = 0; j < segmentPoints.size() - 1; j++) {
                Location from = segmentPoints.get(j);
                Location to = segmentPoints.get(j + 1);

                List<Location> steps = interpolateLinearPath(from, to, 0.5);

                if (!interpolatedSegment.isEmpty()) {
                    steps.remove(0);
                }

                interpolatedSegment.addAll(steps);
            }

            if (!allPoints.isEmpty() && !interpolatedSegment.isEmpty()) {
                interpolatedSegment.remove(0);
            }

            allPoints.addAll(interpolatedSegment);
        }

        return allPoints;
    }


    /**
     * Interpolates a linear path between two locations by adding intermediate points at equal distances.
     * This method calculates points along the straight line between the given start and end locations
     * separated by the specified step distance.
     *
     * @param start the starting location of the path
     * @param end   the ending location of the path
     * @param step  the distance between adjacent points in the generated path
     * @return a list of locations representing the interpolated linear path, including the start and end points
     */
    private List<Location> interpolateLinearPath(Location start, Location end, double step) {
        double dx = end.getX() - start.getX();
        double dy = end.getY() - start.getY();
        double dz = end.getZ() - start.getZ();

        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance == 0) {
            return Collections.singletonList(start);
        }

        int steps = (int) Math.floor(distance / step);

        List<Location> result = new ArrayList<>();

        for (int i = 0; i <= steps; i++) {
            double t = (i * step) / distance;
            double x = start.getX() + dx * t;
            double y = start.getY() + dy * t;
            double z = start.getZ() + dz * t;

            result.add(new Location(start.getWorld(), x, y, z));
        }

        if (!result.isEmpty() && !result.getLast().equals(end)) {
            result.add(end);
        }

        return result;
    }

    public List<Location> findSimplePath(Location start, Location goal, World world) {
        Set<Node> visited = new HashSet<>();
        Node startNode = new Node(start.getBlockX(), start.getBlockY(), start.getBlockZ());
        Node goalNode = new Node(goal.getBlockX(), goal.getBlockY(), goal.getBlockZ());

        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.distanceTo(goalNode)));

        queue.add(startNode);

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            if (current.equals(goalNode)) {
                return reconstructPath(current, world);
            }

            if (visited.contains(current)) continue;
            visited.add(current);

            for (Node neighbor : generateNeighbors(current, world)) {
                if (!visited.contains(neighbor)) {
                    neighbor.parent = current;
                    queue.add(neighbor);
                }
            }
        }

        return Collections.emptyList();
    }



    /**
     * Reconstructs the path from an end node by tracing its parent nodes
     * back to the start and converting each node to a location within the given world.
     *
     * @param end   the end node of the path to be reconstructed
     * @param world the world in which the path is located
     * @return a list of locations representing the reconstructed path from start to end
     */
    private List<Location> reconstructPath(Node end, World world) {
        List<Location> path = new ArrayList<>();
        Node current = end;
        while (current != null) {
            path.add(new Location(world, current.x + 0.5, current.y, current.z + 0.5));
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * Generates a list of neighboring nodes that are traversable from the current node
     * in the given world. This method considers all possible directions, including diagonals,
     * and ensures that the entity's movement follows traversal rules determined by the position constraints.
     *
     * @param current the current node from which neighbors are to be generated
     * @param world   the world in which the traversal is occurring
     * @return a list of neighboring nodes that can be traversed to from the current node
     */
    private List<Node> generateNeighbors(Node current, World world) {
        List<Node> neighbors = new ArrayList<>();

        int[][] directions = {
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1},
                {1, 1},
                {1, -1},
                {-1, 1},
                {-1, -1}
        };

        for (int[] dir : directions) {
            int dx = dir[0];
            int dz = dir[1];

            if (Math.abs(dx) == 1 && Math.abs(dz) == 1) {
                if (!canStandAt(world, current.x + dx, current.y, current.z) ||
                        !canStandAt(world, current.x, current.y, current.z + dz)) {
                    continue;
                }
            }

            if (canStandAt(world, current.x + dx, current.y, current.z + dz)) {
                neighbors.add(new Node(current.x + dx, current.y, current.z + dz));
            } else if (canStandAt(world, current.x + dx, current.y + 1, current.z + dz)) {
                neighbors.add(new Node(current.x + dx, current.y + 1, current.z + dz));
            } else if (canStandAt(world, current.x + dx, current.y - 1, current.z + dz)) {
                neighbors.add(new Node(current.x + dx, current.y - 1, current.z + dz));
            }
        }

        return neighbors;
    }

    /**
     * Determines if an entity can stand at a specified location in the given world.
     * It checks whether the block at the specified position for the entity's feet and head are passable,
     * and whether the block below the feet is a solid block.
     *
     * @param world the world in which the block positions are checked
     * @param x     the x-coordinate of the position to check
     * @param y     the y-coordinate of the position to check
     * @param z     the z-coordinate of the position to check
     * @return true if the entity can stand at the specified location; false otherwise
     */
    private boolean canStandAt(World world, int x, int y, int z) {
        Block feet = world.getBlockAt(x, y, z);
        Block head = world.getBlockAt(x, y + 1, z);
        Block ground = world.getBlockAt(x, y - 1, z);

        return feet.isPassable() && head.isPassable() && ground.getType().isSolid();
    }
}