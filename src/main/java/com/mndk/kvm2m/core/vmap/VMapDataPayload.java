package com.mndk.kvm2m.core.vmap;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;

public class VMapDataPayload extends HashMap<Integer, VMapDataPayload.Record> {

    @RequiredArgsConstructor
    @Getter
    @ToString
    public static class Record {
        private final VMapElementDataType type;
        private final Object[] dataRow;
    }
}