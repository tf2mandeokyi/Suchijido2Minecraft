package com.mndk.kvm2m.core.vmap;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class VMapGeometryPayload<T> {
    private final VMapElementGeomType type;
    private final T geometryData;
}
