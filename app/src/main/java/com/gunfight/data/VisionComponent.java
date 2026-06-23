package com.gunfight.data;

import java.util.HashSet;
import java.util.Set;

public class VisionComponent {
    public float range;
    public float fovDegrees;
    public final Set<Integer> visibleIds;

    public VisionComponent(float range, float fovDegrees) {
        this.range = range;
        this.fovDegrees = fovDegrees;
        this.visibleIds = new HashSet<>(32);
    }
}
