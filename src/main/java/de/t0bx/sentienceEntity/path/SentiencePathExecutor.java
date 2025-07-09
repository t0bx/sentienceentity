package de.t0bx.sentienceEntity.path;

import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.network.wrapper.packets.PacketSetHeadRotation;
import de.t0bx.sentienceEntity.network.wrapper.packets.PacketTeleportEntity;
import de.t0bx.sentienceEntity.network.wrapper.packets.PacketUpdateEntityRotation;
import de.t0bx.sentienceEntity.npc.SentienceNPC;
import de.t0bx.sentienceEntity.path.data.Node;
import de.t0bx.sentienceEntity.path.data.SentiencePath;
import de.t0bx.sentienceEntity.path.data.SentiencePointPath;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Getter
public class SentiencePathExecutor {

    private final int entityId;
    private final SentiencePath path;

    public SentiencePathExecutor(int entityId, SentiencePath path) {
        this.entityId = entityId;
        this.path = path;
    }

    public void preparePath() {
        startPath(ensurePathLoaded(path));
    }

    public void startPath(List<Location> paths) {
        System.out.println("Starting...");
        String npcName = SentienceEntity.getInstance().getNpcshandler().getNpcNameFromId(entityId);
        SentienceNPC npc = SentienceEntity.getInstance().getNpcshandler().getNPC(npcName);

        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (index >= paths.size() - 1) {
                    cancel();
                    sendMovementPacket(npc, npc.getLocation(), npc.getLocation().getYaw(), npc.getLocation().getPitch());
                    return;
                }

                Location current = paths.get(index);
                Location next = paths.get(index + 1);

                float yaw = calculateYaw(current, next);
                float pitch = calculatePitch(current, next);

                sendMovementPacket(npc, next, yaw, pitch);
                sendHeadRotationPacket(npc, yaw);
                index++;
            }
        }.runTaskTimer(SentienceEntity.getInstance(), 0L, 2L);
    }

    public void sendMovementPacket(SentienceNPC npc, Location location, float yaw, float pitch) {
        location.setYaw(yaw);
        location.setPitch(pitch);
        var movePacket = new PacketTeleportEntity(npc.getEntityId(), location, 0, 0, 0, true);

        for (var players : npc.getChannels()) {
            players.sendPacket(movePacket);
        }
    }

    public void sendHeadRotationPacket(SentienceNPC npc, float yaw) {
        var headRotationPacket = new PacketSetHeadRotation(npc.getEntityId(), yaw);

        for (var players : npc.getChannels()) {
            players.sendPacket(headRotationPacket);
        }
    }

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
                    steps.removeFirst();
                }

                interpolatedSegment.addAll(steps);
            }

            if (!allPoints.isEmpty()) {
                interpolatedSegment.removeFirst();
            }

            allPoints.addAll(interpolatedSegment);
        }

        return allPoints;
    }

    private List<Location> interpolateLinearPath(SentiencePointPath from, SentiencePointPath to, double step) {
        Location start = from.getLocation();
        Location end = to.getLocation();

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

    public float calculateYaw(Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();

        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        return yaw < 0 ? yaw + 360 : yaw;
    }

    public float calculatePitch(Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double dz = to.getZ() - from.getZ();

        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        return (float) -Math.toDegrees(Math.atan2(dy, horizontalDistance));
    }

    public List<Location> findSimplePath(Location start, Location goal, World world) {
        Queue<Node> queue = new LinkedList<>();
        Set<Node> visited = new HashSet<>();

        Node startNode = new Node(start.getBlockX(), start.getBlockY(), start.getBlockZ());
        Node goalNode = new Node(goal.getBlockX(), goal.getBlockY(), goal.getBlockZ());

        queue.add(startNode);
        visited.add(startNode);

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            if (current.equals(goalNode)) {
                return reconstructPath(current, world);
            }

            for (Node neighbor : generateNeighbors(current, world)) {
                if (!visited.contains(neighbor)) {
                    neighbor.parent = current;
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return Collections.emptyList();
    }

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

    private List<Node> generateNeighbors(Node current, World world) {
        List<Node> neighbors = new ArrayList<>();

        int[][] directions = {
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1}
        };

        for (int[] dir : directions) {
            int dx = dir[0];
            int dz = dir[1];

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

    private boolean canStandAt(World world, int x, int y, int z) {
        Block feet = world.getBlockAt(x, y, z);
        Block head = world.getBlockAt(x, y + 1, z);
        Block ground = world.getBlockAt(x, y - 1, z);

        return feet.isPassable() && head.isPassable() && ground.getType().isSolid();
    }
}
