package com.esri.core.geometry;

public enum SimpleStateEnum {
    // on creation, after projection and after generalization a geometry has state simple unknown (not know if simple or not)
    SIMPLE_UNKNOWN,
    // weak simple (no self intersections, ring orientation is correct, but ring order is not)
    WEAK_SIMPLE,
    // same as weak simple + OGC ring order.
    STRONG_SIMPLE,
    // is_simple method has been run on the geometry and it is known to be non-simple, but the reason is unknown
    NON_SIMPLE,
    // non-simple, because the structure is bad (0 size path, for example).
    STRUCTURE_FLAW,
    // Non-simple, because there are degenerate segments.
    DEGENERATE_SEGMENTS,
    // Non-simple, because not clustered properly, that is there are non-coincident vertices closer than tolerance.
    CLUSTERING,
    // Non-simple, because not cracked properly (intersecting segments, overlaping segments)
    CRACKING,
    // Non-simple, because there are crossovers (self intersections that are not cracking case).
    CROSS_OVER,
    // Non-simple, because holes or exteriors have wrong orientation.
    RING_ORIENTATION,
}
