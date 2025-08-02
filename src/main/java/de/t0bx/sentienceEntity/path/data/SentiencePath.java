package de.t0bx.sentienceEntity.path.data;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class SentiencePath {
    private final String name;
    private final SentiencePathType type;
    private Map<Integer, SentiencePointPath> paths = new HashMap<>();
}
