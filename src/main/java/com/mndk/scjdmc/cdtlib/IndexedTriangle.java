package com.mndk.scjdmc.cdtlib;

import com.mndk.scjdmc.util.math.Vector2DH;
import com.mndk.scjdmc.util.shape.Triangle;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Use IndexedTriangle::toTriangle() to get coordinates
 */
@RequiredArgsConstructor
public class IndexedTriangle {

    public final int v0, v1, v2, n0, n1, n2;

    public Triangle toTriangle(List<Vector2DH> vertices) {
        return new Triangle(vertices.get(v0), vertices.get(v1), vertices.get(v2));
    }

    @Override public String toString() {
        return String.format("IndexedTriangle{v=[%d, %d, %d], n=[%d, %d, %d]}", v0, v1, v2, n0, n1, n2);
    }
}
