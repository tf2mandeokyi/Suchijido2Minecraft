package com.mndk.scjdmc.cdtlib;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IndexedEdge {

    public final int v0, v1;

    @Override public String toString() {
        return String.format("IndexedEdge[%d, %d]", v0, v1);
    }
}
