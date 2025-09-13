package de.t0bx.sentienceEntity.path.data;

import lombok.Data;

import java.util.Objects;

@Data
public class Node {
    public int x, y, z;
    public Node parent;

    public Node(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Node n)) return false;
        return x == n.x && y == n.y && z == n.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    public double distanceTo(Node other) {
        return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2) + Math.pow(z - other.z, 2));
    }
}
