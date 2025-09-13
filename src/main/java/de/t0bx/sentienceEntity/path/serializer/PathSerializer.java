package de.t0bx.sentienceEntity.path.serializer;

import de.t0bx.sentienceEntity.SentienceEntity;
import org.bukkit.Location;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class PathSerializer {

    /**
     * Saves a path consisting of a list of {@link Location} objects to a file.
     * The file is saved in a designated folder within the plugin's data directory
     * using the specified path name. The method serializes the path into
     * {@link SerializableLocation} objects before persisting it.
     * Additionally, the file is marked as hidden on systems supporting the "dos:hidden" attribute.
     *
     * @param pathName The name to be used for saving the path file. This name will be suffixed with ".dat".
     * @param path A list of {@link Location} objects representing the path to be saved.
     * @throws IOException If an error occurs while creating directories, writing the file, or setting file attributes.
     */
    public static synchronized void savePath(String pathName, List<Location> path) throws IOException {
        Path pathFile = Paths.get(
                SentienceEntity.getInstance().getDataFolder().getAbsolutePath(),
                "paths/saved/",
                pathName + ".dat"
        );
        Files.createDirectories(pathFile.getParent());

        List<SerializableLocation> serializableList = path.stream()
                .map(SerializableLocation::new)
                .toList();

        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(pathFile))) {
            oos.writeObject(serializableList);
        }

        try {
            Files.setAttribute(pathFile, "dos:hidden", true);
        } catch (UnsupportedOperationException ignored) {

        }
    }

    /**
     * Loads a path consisting of {@link Location} objects from a file.
     * The file is read from a designated folder within the plugin's data directory
     * based on the specified path name. The method deserializes the path data
     * stored as {@link SerializableLocation} objects into {@link Location} objects.
     *
     * @param pathName The name of the path file to be loaded. This name should be suffixed with ".dat".
     * @return A list of {@link Location} objects representing the loaded path, or null if the path file does not exist
     *         or cannot be loaded.
     * @throws IOException If an error occurs while reading the file.
     */
    @SuppressWarnings("unchecked")
    public static synchronized List<Location> loadPath(String pathName) throws IOException {
        Path pathFile = Paths.get(
                SentienceEntity.getInstance().getDataFolder().getAbsolutePath(),
                "paths/saved/",
                pathName + ".dat"
        );
        if (!Files.exists(pathFile)) return null;

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(pathFile))) {
            List<SerializableLocation> serializableList = (List<SerializableLocation>) ois.readObject();
            return serializableList.stream()
                    .map(SerializableLocation::toBukkitLocation)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (ClassNotFoundException exception) {
            SentienceEntity.getInstance().getLogger().log(Level.WARNING, "Could not load path " + pathName + "!, ", exception);
            return null;
        }
    }

    /**
     * Removes a saved path file from the designated storage location.
     * The path file corresponds to the specified path name and is deleted
     * if it exists in the "paths/saved" directory within the plugin's data folder.
     *
     * @param pathName The name of the path file to be removed. The file is expected to
     *                 have a ".dat" extension appended to this name.
     * @throws IOException If an error occurs while attempting to delete the file.
     */
    public static synchronized void removePath(String pathName) throws IOException {
        Path pathFile = Paths.get(
                SentienceEntity.getInstance().getDataFolder().getAbsolutePath(),
                "paths/saved/",
                pathName + ".dat"
        );
        if (Files.exists(pathFile)) Files.delete(pathFile);
    }
}
