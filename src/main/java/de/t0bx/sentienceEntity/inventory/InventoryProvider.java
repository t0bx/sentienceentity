package de.t0bx.sentienceEntity.inventory;

import de.t0bx.sentienceEntity.utils.item.ItemProvider;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryProvider {
    private final ConcurrentHashMap<UUID, Map<String, Inventory>> playerInventories;

    public InventoryProvider() {
        this.playerInventories = new ConcurrentHashMap<>();
    }

    /**
     * Retrieves or creates an inventory for the specified player. If the inventory
     * with the provided name does not exist for the player, it will create a new
     * one with the specified size and title.
     *
     * @param player the player for whom the inventory is being retrieved or created
     * @param inventoryName the name of the inventory to retrieve or create
     * @param inventorySize the size of the inventory, typically a multiple of 9
     * @param inventoryTitle the title of the inventory to be displayed to the player
     * @return the player's inventory corresponding to the specified name
     */
    public Inventory getInventory(Player player, String inventoryName, int inventorySize, String inventoryTitle) {
        UUID playerUUID = player.getUniqueId();

        if (!playerInventories.containsKey(playerUUID)) {
            playerInventories.put(playerUUID, new HashMap<>());
        }

        Map<String, Inventory> inventories = playerInventories.get(playerUUID);

        if (!inventories.containsKey(inventoryName)) {
            Inventory newInventory = Bukkit.createInventory(player, inventorySize, MiniMessage.miniMessage().deserialize(inventoryTitle));
            inventories.put(inventoryName, newInventory);
        }

        return inventories.get(inventoryName);
    }

    /**
     * Opens an inventory for the specified player. If the inventory with the given
     * name does not already exist for the player, it will be created with the
     * given size and title.
     *
     * @param player the player for whom the inventory is being opened
     * @param inventoryName the name of the inventory to open or create
     * @param inventorySize the size of the inventory, typically a multiple of 9
     * @param inventoryTitle the title of the inventory to be displayed to the player
     */
    public void openInventory(Player player, String inventoryName, int inventorySize, String inventoryTitle) {
        Inventory inventory = getInventory(player, inventoryName, inventorySize, inventoryTitle);
        player.openInventory(inventory);
    }

    /**
     * Removes the specified inventory associated with a player.
     * If the inventory with the given name exists for the player, it will be removed.
     *
     * @param player the player whose inventory is to be removed
     * @param inventoryName the name of the inventory to be removed
     */
    public void removeInventory(Player player, String inventoryName) {
        UUID playerUUID = player.getUniqueId();
        if (playerInventories.containsKey(playerUUID)) {
            playerInventories.get(playerUUID).remove(inventoryName);
        }
    }

    /**
     * Removes all inventories associated with the specified player.
     *
     * @param player the player whose inventories are to be removed
     */
    public void removeAllInventories(Player player) {
        playerInventories.remove(player.getUniqueId());
    }

    /**
     * Sets a placeholder item in specific slots of the provided inventory.
     * This method creates a placeholder item using the specified material,
     * and assigns it to the designated slots in the inventory.
     *
     * @param inventory the inventory where the placeholder item will be set
     * @param material the material used to create the placeholder item
     * @param slots the slots in the inventory where the placeholder item will be placed
     */
    public void setPlaceHolder(Inventory inventory, Material material, int... slots) {
        ItemStack item = new ItemProvider(material).setName(" ").build();
        for (int slot : slots) {
            inventory.setItem(slot, item);
        }
    }

    /**
     * Sets a placeholder item in specific slots of the provided inventory.
     * This method creates a placeholder item using the specified material,
     * and assigns it to the designated slots in the inventory.
     *
     * @param inventory the inventory where the placeholder item will be set
     * @param material the material used to create the placeholder item
     * @param slots the slots in the inventory where the placeholder item will be placed
     */
    public void setPlaceHolder(Inventory inventory, Material material, Set<Integer> slots) {
        ItemStack item = new ItemProvider(material).setName(" ").build();
        for (int slot : slots) {
            inventory.setItem(slot, item);
        }
    }

    /**
     * Computes the set of slot indices that form the border of a grid
     * with the given number of rows and columns.
     *
     * @param rows the total number of rows in the grid
     * @param columns the total number of columns in the grid
     * @return a set of integers representing the indices of border slots in the grid
     */
    public Set<Integer> getBorderSlots(int rows, int columns) {
        Set<Integer> borderSlots = new HashSet<>();

        for (int col = 0; col < columns; col++) {
            borderSlots.add(col);
        }

        for (int col = 0; col < columns; col++) {
            borderSlots.add((rows - 1) * columns + col);
        }

        for (int row = 1; row < rows - 1; row++) {
            borderSlots.add(row * columns);
            borderSlots.add(row * columns + columns - 1);
        }

        return borderSlots;
    }

    /**
     * Computes the set of slot indices that represent the center slots in a grid
     * with the given number of rows and columns. Center slots are all the slots
     * that are not part of the border (i.e., not in the first or last row and column).
     *
     * @param rows the total number of rows in the grid
     * @param columns the total number of columns in the grid
     * @return a set of integers representing the indices of center slots in the grid
     */
    public Set<Integer> getCenterSlots(int rows, int columns) {
        Set<Integer> centerSlots = new HashSet<>();

        for (int row = 1; row < rows - 1; row++) {
            for (int col = 1; col < columns - 1; col++) {
                centerSlots.add(row * columns + col);
            }
        }

        return centerSlots;
    }

    /**
     * Computes the set of slot indices for a specified row in a grid.
     * Each slot index is calculated based on the row number and the total
     * number of columns in the grid.
     *
     * @param row the row index for which slot indices are to be computed (0-based)
     * @param columns the total number of columns in the grid
     * @return a set of integers representing the indices of slots in the specified row
     */
    public Set<Integer> getRowSlots(int row, int columns) {
        Set<Integer> rowSlots = new HashSet<>();

        for (int col = 0; col < columns; col++) {
            rowSlots.add(row * columns + col);
        }

        return rowSlots;
    }

    /**
     * Computes the set of slot indices for a specified column in a grid.
     * Each slot index is calculated based on the column number, the total number
     * of rows, and the total number of columns in the grid.
     *
     * @param column the column index for which slot indices are to be computed (0-based)
     * @param rows the total number of rows in the grid
     * @param columns the total number of columns in the grid
     * @return a set of integers representing the indices of slots in the specified column
     */
    public Set<Integer> getColumnSlots(int column, int rows, int columns) {
        Set<Integer> columnSlots = new HashSet<>();

        for (int row = 0; row < rows; row++) {
            columnSlots.add(row * columns + column);
        }

        return columnSlots;
    }

    /**
     * Computes the set of slot indices that represent the corner slots in a grid
     * with the given number of rows and columns. Corner slots are the top-left,
     * top-right, bottom-left, and bottom-right cells of the grid.
     *
     * @param rows the total number of rows in the grid
     * @param columns the total number of columns in the grid
     * @return a set of integers representing the indices of corner slots in the grid
     */
    public Set<Integer> getCornerSlots(int rows, int columns) {
        Set<Integer> cornerSlots = new HashSet<>();

        cornerSlots.add(0);
        cornerSlots.add(columns - 1);
        cornerSlots.add((rows - 1) * columns);
        cornerSlots.add((rows - 1) * columns + columns - 1);

        return cornerSlots;
    }

    /**
     * Computes the set of slot indices that form a rectangular region in a grid.
     * The rectangle is defined by its starting and ending rows and columns within
     * the grid. Each slot index is calculated based on the row, column, and
     * total number of columns in the grid.
     *
     * @param startRow the starting row index of the rectangle (inclusive, 0-based)
     * @param endRow the ending row index of the rectangle (inclusive, 0-based)
     * @param startCol the starting column index of the rectangle (inclusive, 0-based)
     * @param endCol the ending column index of the rectangle (inclusive, 0-based)
     * @param columns the total number of columns in the grid
     * @return a set of integers representing the indices of the slots within the rectangle
     */
    public Set<Integer> getRectangleSlots(int startRow, int endRow, int startCol, int endCol, int columns) {
        Set<Integer> rectangleSlots = new HashSet<>();

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                rectangleSlots.add(row * columns + col);
            }
        }

        return rectangleSlots;
    }

    /**
     * Computes the set of slot indices that form a checkerboard pattern in a grid
     * with the specified number of rows and columns. The pattern alternates between
     * true and false slots based on the starting value provided.
     *
     * @param rows the total number of rows in the grid
     * @param columns the total number of columns in the grid
     * @param startWithTrue a boolean indicating whether the top-left corner slot
     *                      should be part of the checkerboard (true) or not (false)
     * @return a set of integers representing the indices of the slots forming the
     *         checkerboard pattern
     */
    public Set<Integer> getCheckerboardSlots(int rows, int columns, boolean startWithTrue) {
        Set<Integer> checkerSlots = new HashSet<>();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                boolean isCheckerSlot = (row + col) % 2 == 0;
                if (startWithTrue == isCheckerSlot) {
                    checkerSlots.add(row * columns + col);
                }
            }
        }

        return checkerSlots;
    }

    /**
     * Computes the set of slot indices that form an "X" pattern within a grid
     * with the specified number of rows and columns. An "X" pattern includes
     * both the main diagonal (from top-left to bottom-right) and the anti-diagonal
     * (from top-right to bottom-left) slots.
     *
     * @param rows the total number of rows in the grid
     * @param columns the total number of columns in the grid
     * @return a set of integers representing the indices of the slots forming the "X" pattern
     */
    public Set<Integer> getXPatternSlots(int rows, int columns) {
        Set<Integer> xSlots = new HashSet<>();

        int minDimension = Math.min(rows, columns);

        for (int i = 0; i < minDimension; i++) {
            xSlots.add(i * columns + i);
        }

        for (int i = 0; i < minDimension; i++) {
            xSlots.add(i * columns + (columns - 1 - i));
        }

        return xSlots;
    }

    /**
     * Computes the set of slot indices that form a "plus" pattern within a grid
     * with the specified number of rows and columns. A "plus" pattern includes
     * all slots in the central row and the central column of the grid.
     *
     * @param rows the total number of rows in the grid
     * @param columns the total number of columns in the grid
     * @return a set of integers representing the indices of the slots forming the "plus" pattern
     */
    public Set<Integer> getPlusPatternSlots(int rows, int columns) {
        Set<Integer> plusSlots = new HashSet<>();

        int centerRow = rows / 2;
        int centerCol = columns / 2;

        for (int col = 0; col < columns; col++) {
            plusSlots.add(centerRow * columns + col);
        }

        for (int row = 0; row < rows; row++) {
            plusSlots.add(row * columns + centerCol);
        }

        return plusSlots;
    }

    /**
     * Retrieves the set of slot indices for a specific ring within a grid defined by rows and columns.
     * A ring is formed by the outermost layer and inward layers of the grid, starting at `ringIndex`.
     *
     * @param rows the number of rows in the grid
     * @param columns the number of columns in the grid
     * @param ringIndex the index of the ring to retrieve, where 0 represents the outermost ring
     * @return a set of integer indices representing the slots in the specified ring;
     *         returns an empty set if the ring index is invalid or lies outside the grid boundary
     */
    public Set<Integer> getRingSlots(int rows, int columns, int ringIndex) {
        Set<Integer> ringSlots = new HashSet<>();

        if (ringIndex >= Math.min(rows, columns) / 2) {
            return ringSlots;
        }

        int endRow = rows - 1 - ringIndex;
        int endCol = columns - 1 - ringIndex;

        for (int col = ringIndex; col <= endCol; col++) {
            ringSlots.add(ringIndex * columns + col);
        }

        if (endRow != ringIndex) {
            for (int col = ringIndex; col <= endCol; col++) {
                ringSlots.add(endRow * columns + col);
            }
        }

        for (int row = ringIndex + 1; row < endRow; row++) {
            ringSlots.add(row * columns + ringIndex);
        }

        if (endCol != ringIndex) {
            for (int row = ringIndex + 1; row < endRow; row++) {
                ringSlots.add(row * columns + endCol);
            }
        }

        return ringSlots;
    }

    /**
     * Fills the specified slots of an inventory alternately with items of two given materials.
     *
     * @param inventory The inventory to be modified.
     * @param material1 The first material to be alternated.
     * @param material2 The second material to be alternated.
     * @param slots A set of integer slot indices to be filled alternately.
     */
    public void fillAlternating(Inventory inventory, Material material1, Material material2, Set<Integer> slots) {
        List<Integer> slotList = new ArrayList<>(slots);
        Collections.sort(slotList);

        for (int i = 0; i < slotList.size(); i++) {
            Material material = (i % 2 == 0) ? material1 : material2;
            ItemStack item = new ItemProvider(material).setName(" ").build();
            inventory.setItem(slotList.get(i), item);
        }
    }

    /**
     * Creates a layered border in the specified inventory using the given materials.
     * Each material provided is used to create a layer starting from the outermost layer
     * and moving inwards. The number of rows and columns define the dimensions of the inventory,
     * and the layers are calculated based on these dimensions.
     *
     * @param inventory the inventory where the layered border will be created
     * @param rows the number of rows in the inventory
     * @param columns the number of columns in the inventory
     * @param materials the materials to be used for each layer of the border, starting from the outermost
     */
    public void createLayeredBorder(Inventory inventory, int rows, int columns, Material... materials) {
        for (int layer = 0; layer < materials.length && layer < Math.min(rows, columns) / 2; layer++) {
            Set<Integer> ringSlots = getRingSlots(rows, columns, layer);
            setPlaceHolder(inventory, materials[layer], ringSlots);
        }
    }

    /**
     * Calculates the number of rows and columns based on the given inventory size.
     *
     * @param inventorySize the total size of the inventory
     * @return an array where the first element is the number of rows and the second element is the number of columns
     */
    public int[] getRowsAndColumns(int inventorySize) {
        int rows = inventorySize / 9;
        int columns = 9;
        return new int[]{rows, columns};
    }
}
