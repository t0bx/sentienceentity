package de.t0bx.sentienceEntity.path;

import com.google.gson.JsonObject;
import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.path.data.SentiencePath;
import de.t0bx.sentienceEntity.path.data.SentiencePointPath;
import de.t0bx.sentienceEntity.utils.JsonDocument;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * Applies a pre-defined sentience path to the specified entity.
     *
     * This method retrieves a `SentiencePath` object associated with the given
     * path name from the cache. If the path exists, it initializes a
     * `SentiencePathExecutor` for the specified entity and calls its preparation logic.
     *
     * @param entityId The unique identifier of the entity to which the path will be applied.
     * @param pathName The name of the sentience path to be applied.
     */
    public void applyPath(int entityId, String pathName) {
        SentiencePath path = this.cachedPaths.getOrDefault(pathName, null);
        if (path == null) return;

        SentiencePathExecutor executor = new SentiencePathExecutor(entityId, path);
        executor.preparePath();
    }

    /**
     * Creates a new sentience path with the specified name and caches it.
     *
     * This method checks if a path with the given name already exists in the cache.
     * If the path already exists, it throws an IllegalArgumentException. Otherwise,
     * it creates a new {@code SentiencePath} and adds it to the cache.
     *
     * @param pathName The name of the new sentience path to be created.
     *                 Must be unique and not already present in the cache.
     * @throws IllegalArgumentException If a path with the specified name already exists.
     */
    public void createPath(String pathName) {
        if (this.cachedPaths.containsKey(pathName))
            throw new IllegalArgumentException("Path with name " + pathName + " already exists!");

        SentiencePath path = new SentiencePath(pathName);
        this.cachedPaths.put(pathName, path);
    }

    /**
     * Removes a sentience path identified by the specified name from the cache.
     *
     * This method checks if a path with the given name exists in the cache. If the path
     * does not exist, it throws an {@code IllegalArgumentException}. If the path exists,
     * it is removed from the cache.
     *
     * @param pathName The name of the sentience path to be removed.
     *                 Must correspond to an existing path in the cache.
     * @throws IllegalArgumentException If a path with the specified name does not exist.
     */
    public void removePath(String pathName) {
        if (!this.cachedPaths.containsKey(pathName))
            throw new IllegalArgumentException("Path with name " + pathName + " does not exist!");

        this.cachedPaths.remove(pathName);
    }

    /**
     * Checks whether a sentience path with the specified name exists in the cache.
     *
     * This method verifies if the provided path name is present in the cached paths map.
     *
     * @param pathName The name of the sentience path to check for existence.
     *                 Must not be null or empty.
     * @return {@code true} if the path with the specified name exists in the cache,
     *         {@code false} otherwise.
     */
    public boolean doesPathNameExist(String pathName) {
        return this.cachedPaths.containsKey(pathName);
    }

    /**
     * Checks if a specific index exists in the path identified by the provided path name.
     *
     * This method retrieves the {@code SentiencePath} associated with the given name
     * from the cache and verifies if its path map contains the specified index.
     *
     * @param pathName The name of the sentience path to be checked. Must not be null.
     * @param index The index to check for existence in the path.
     * @return {@code true} if the specified index exists in the path, {@code false} otherwise.
     */
    public boolean hasPathIndex(String pathName, int index) {
        SentiencePath path = this.cachedPaths.getOrDefault(pathName, null);
        if (path == null) return false;

        return path.getPaths().containsKey(index);
    }

    /**
     * Adds a new point to an existing sentience path identified by the provided name.
     *
     * This method retrieves the `SentiencePath` associated with the specified path name
     * from the cache. If the path does not exist, it throws an `IllegalArgumentException`.
     * A new `SentiencePointPath` is created and initialized with the specified location and
     * teleport flag, then added to the list of points in the retrieved path. The new point
     * is also saved to the respective file for persistence.
     *
     * @param pathName The name of the sentience path to which the point will be added.
     *                 Must correspond to an existing path in the cache.
     * @param location An instance of `Location` that represents the coordinates of the new point.
     *                 Must not be null.
     * @param isTeleport A boolean indicating whether the point represents a teleport location.
     *                   If true, it marks this point as a teleport point.
     * @throws IllegalArgumentException If no path with the specified name exists in the cache.
     */
    public void addPoint(String pathName, Location location, boolean isTeleport) {
        SentiencePath path = this.cachedPaths.getOrDefault(pathName, null);
        if (path == null) throw new IllegalArgumentException("Path with name " + pathName + " does not exist!");

        SentiencePointPath pointPath = new SentiencePointPath();
        pointPath.setLocation(location);
        pointPath.setTeleport(isTeleport);

        path.getPaths().put(path.getPaths().size(), pointPath);
        savePointToFile(pathName, pointPath);
    }

    /**
     * Removes a point at the specified index from the sentience path identified by the given path name.
     *
     * This method updates the internal representation of the path by removing the point
     * at the specified index, reindexing the remaining points, and clearing the original map.
     * It also ensures the removal is persisted to the external storage.
     *
     * @param pathName The name of the sentience path from which the point will be removed.
     *                 Must correspond to an existing path in the cache.
     * @param index The index of the point to be removed. Must be within the bounds of the path's points.
     * @throws IllegalArgumentException If the path with the specified name does not exist, or if the index is out of bounds.
     */
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

    /**
     * Retrieves the specified {@code SentiencePath} from the cached paths.
     *
     * This method looks up the cache for a sentience path name and returns the associated
     * {@code SentiencePath} object if found. If the specified path name does not exist
     * in the cache, it returns {@code null}.
     *
     * @param pathName The name of the sentience path to retrieve. Must not be null.
     * @return The {@code SentiencePath} associated with the given path name, or {@code null}
     *         if no such path exists in the cache.
     */
    @SuppressWarnings("unused")
    public SentiencePath getPath(String pathName) {
        return this.cachedPaths.getOrDefault(pathName, null);
    }

    /**
     * Retrieves the points associated with a specified sentience path.
     *
     * This method fetches the {@code Map<Integer, SentiencePointPath>} that represents the
     * points in the sentience path identified by the given path name. If the specified
     * path name is not found in the cache, it returns {@code null}.
     *
     * @param pathName The name of the sentience path whose points are to be retrieved.
     *                 Must correspond to an existing path in the cache. Must not be null.
     * @return A map where the keys are point indices and the values are {@code SentiencePointPath} objects
     *         representing the points in the path. Returns {@code null} if the path does not exist in the cache.
     */
    public Map<Integer, SentiencePointPath> getPoints(String pathName) {
        return this.cachedPaths.getOrDefault(pathName, null).getPaths();
    }

    /**
     * Retrieves an unmodifiable view of all cached sentience paths.
     *
     * This method provides access to the internal map of sentience paths,
     * where the keys are path names and the values are the corresponding
     * {@code SentiencePath} objects. The returned map is immutable, ensuring
     * that its contents cannot be altered externally.
     *
     * @return A map containing all cached sentience paths. The keys represent
     *         the names of the paths, and the values are the associated {@code SentiencePath}
     *         objects. The returned map is unmodifiable.
     */
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
