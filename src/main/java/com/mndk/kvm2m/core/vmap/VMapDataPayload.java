package com.mndk.kvm2m.core.vmap;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class VMapDataPayload {
    private final VMapElementDataType type;
    private final Object[] dataRow;
}