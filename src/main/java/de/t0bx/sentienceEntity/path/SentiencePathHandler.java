package de.t0bx.sentienceEntity.path;

import com.google.gson.JsonObject;
import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.path.data.SentiencePath;
import de.t0bx.sentienceEntity.path.data.SentiencePointPath;
import de.t0bx.sentienceEntity.utils.JsonDocument;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"ResultOfMethodCallIgnored", "CallToPrintStackTrace"})
public class SentiencePathHandler {

    private final Map<String, SentiencePath> cachedPaths;
    private final Map<Integer, SentiencePathExecutor> inPath;
    private JsonDocument jsonDocument;

    public SentiencePathHandler() {
        this.cachedPaths = new ConcurrentHashMap<>();
        this.inPath = new ConcurrentHashMap<>();
        this.loadPathsFromFile();
    }

    public void applyPath(int entityId, String pathName) {
        SentiencePath path = this.cachedPaths.getOrDefault(pathName, null);
        if (path == null) return;

        SentiencePathExecutor executor = new SentiencePathExecutor(entityId, path);
        executor.preparePath();
    }

    public void createPath(String pathName) {
        if (this.cachedPaths.containsKey(pathName))
            throw new IllegalArgumentException("Path with name " + pathName + " already exists!");

        SentiencePath path = new SentiencePath(pathName);
        this.cachedPaths.put(pathName, path);
    }

    public void removePath(String pathName) {
        if (!this.cachedPaths.containsKey(pathName))
            throw new IllegalArgumentException("Path with name " + pathName + " does not exist!");

        this.cachedPaths.remove(pathName);
    }

    public boolean doesPathNameExist(String pathName) {
        return this.cachedPaths.containsKey(pathName);
    }

    public boolean hasPathIndex(String pathName, int index) {
        SentiencePath path = this.cachedPaths.getOrDefault(pathName, null);
        if (path == null) return false;

        return path.getPaths().containsKey(index);
    }

    public void addPoint(String pathName, Location location, boolean isTeleport) {
        SentiencePath path = this.cachedPaths.getOrDefault(pathName, null);
        if (path == null) throw new IllegalArgumentException("Path with name " + pathName + " does not exist!");

        SentiencePointPath pointPath = new SentiencePointPath();
        pointPath.setLocation(location);
        pointPath.setTeleport(isTeleport);

        path.getPaths().put(path.getPaths().size(), pointPath);
        savePointToFile(pathName, pointPath);
    }

    public void removePoint(String pathName, int index) {
        SentiencePath path = this.cachedPaths.getOrDefault(pathName, null);
        if (path == null) throw new IllegalArgumentException("Path with name " + pathName + " does not exist!");

        List<Integer> points = List.copyOf(path.getPaths().keySet());
        if (index < 0 || index > points.size())
            throw new IllegalArgumentException("Index " + index + " is out of bounds!");

        Map<Integer, SentiencePointPath> oldMap = path.getPaths();
        Map<Integer, SentiencePointPath> newMap = new LinkedHashMap<>();

        int newIndex = 0;
        for (Integer key : points) {
            if (key == index) continue;
            newMap.put(newIndex++, oldMap.get(key));
        }

        oldMap.clear();
        path.setPaths(newMap);

        removePointFromFile(pathName, index);
    }

    @SuppressWarnings("unused")
    public SentiencePath getPath(String pathName) {
        return this.cachedPaths.getOrDefault(pathName, null);
    }

    public Map<Integer, SentiencePointPath> getPoints(String pathName) {
        return this.cachedPaths.getOrDefault(pathName, null).getPaths();
    }

    public Map<String, SentiencePath> getPaths() {
        return Collections.unmodifiableMap(this.cachedPaths);
    }
    
    private void loadPathsFromFile() {
        File pathsFolder = new File(SentienceEntity.getInstance().getDataFolder(), "paths/");
        if (!pathsFolder.exists()) pathsFolder.mkdirs();

        File[] files = pathsFolder.listFiles();
        if (files == null) return;

        this.cachedPaths.clear();
        for (File file : files) {
            if (!file.getName().endsWith(".json")) continue;

            try {
                this.jsonDocument = JsonDocument.loadDocument(file);
                if (this.jsonDocument == null) continue;

                JsonObject pathObject = this.jsonDocument.getJsonObject();
                if (pathObject == null) continue;

                String pathName = file.getName().substring(0, file.getName().length() - 5);
                SentiencePath path = new SentiencePath(pathName);
                path.getPaths().clear();

                int index = 0;
                while (pathObject.has("point_" + index)) {
                    JsonObject pointObject = pathObject.get("point_" + index).getAsJsonObject();
                    if (pointObject == null) continue;

                    SentiencePointPath pointPath = new SentiencePointPath();
                    Location location = new Location(Bukkit.getWorld(pointObject.get("world").getAsString()),
                            pointObject.get("x").getAsDouble(),
                            pointObject.get("y").getAsDouble(),
                            pointObject.get("z").getAsDouble(),
                            pointObject.get("yaw").getAsFloat(),
                            pointObject.get("pitch").getAsFloat()
                    );
                    pointPath.setLocation(location);

                    boolean isTeleport = pointObject.get("isTeleport").getAsBoolean();
                    pointPath.setTeleport(isTeleport);

                    path.getPaths().put(index, pointPath);

                    index++;
                }

                this.cachedPaths.put(pathName, path);
            } catch (Exception exception) {
                SentienceEntity.getInstance().getLogger().severe("Failed to load path '" + file.getName() + "': " + exception.getMessage());
                exception.printStackTrace();
            }
        }
    }

    private void savePointToFile(String pathName, SentiencePointPath pointPath) {
        File tempFile = new File(SentienceEntity.getInstance().getDataFolder(), "paths/" + pathName + ".json");
        try {
            this.jsonDocument = JsonDocument.loadDocument(tempFile);

            if (this.jsonDocument == null) {
                this.jsonDocument = new JsonDocument();
            }

            JsonObject pathObject = this.jsonDocument.getJsonObject();
            if (pathObject == null) {
                pathObject = new JsonObject();
                this.jsonDocument.setJsonObject(pathObject);
            }

            int nextIndex = 0;
            while (this.jsonDocument.getJsonObject().has("point_" + nextIndex)) {
                nextIndex++;
            }

            JsonObject pointObject = getJsonObject(pointPath);

            pathObject.add("point_" + nextIndex, pointObject);

            this.jsonDocument.save(tempFile);
        } catch (IOException exception) {
            SentienceEntity.getInstance().getLogger().severe("Failed to save path: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    private @NotNull JsonObject getJsonObject(SentiencePointPath pointPath) {
        JsonObject pointObject = new JsonObject();
        pointObject.addProperty("x", pointPath.getLocation().getX());
        pointObject.addProperty("y", pointPath.getLocation().getY());
        pointObject.addProperty("z", pointPath.getLocation().getZ());
        pointObject.addProperty("yaw", pointPath.getLocation().getYaw());
        pointObject.addProperty("pitch", pointPath.getLocation().getPitch());
        pointObject.addProperty("world", pointPath.getLocation().getWorld().getName());

        pointObject.addProperty("isTeleport", pointPath.isTeleport());
        return pointObject;
    }

    private void removePointFromFile(String pathName, int index) {
        File tempFile = new File(SentienceEntity.getInstance().getDataFolder(), "paths/" + pathName + ".json");
        try {
            this.jsonDocument = JsonDocument.loadDocument(tempFile);
            if (this.jsonDocument == null) return;

            JsonObject pathObject = this.jsonDocument.getJsonObject();
            if (pathObject == null) return;

            if (!pathObject.has("point_" + index)) return;

            pathObject.remove("point_" + index);

            int i = index + 1;
            while (pathObject.has("point_" + i)) {
                JsonObject pointObject = pathObject.get("point_" + i).getAsJsonObject();
                pathObject.remove("point_" + i);
                pathObject.add("point_" + (i - 1), pointObject);
                i++;
            }

            this.jsonDocument.save(tempFile);
        } catch (IOException exception) {
            SentienceEntity.getInstance().getLogger().severe("Failed to remove point from path: " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}
