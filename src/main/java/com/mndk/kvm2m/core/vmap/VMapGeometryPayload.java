package com.mndk.kvm2m.core.vmap;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;

public class VMapGeometryPayload extends HashMap<Long, VMapGeometryPayload.Record<?>> {

    @RequiredArgsConstructor
    @Getter
    @ToString
    public static class Record<T> {
        private final VMapElementGeomType type;
        private final T geometryData;
    }
}
