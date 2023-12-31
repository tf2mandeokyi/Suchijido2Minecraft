package com.mndk.scjdmc.cdtlib;

import java.util.Objects;

public record IndexedEdge(int v0, int v1) {
    @Override
    public String toString() {
        return String.format("IndexedEdge[%d, %d]", v0, v1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexedEdge that = (IndexedEdge) o;
        return v0 == that.v0 && v1 == that.v1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(v0, v1);
    }
}
