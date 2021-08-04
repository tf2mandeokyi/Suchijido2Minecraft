package com.mndk.scjd2mc.core.scjd.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ElementGeometryType {

    NULL("Null"),
    POINT("Point"),
    LINESTRING("LineString"),
    MULTILINESTRING("MultiLineString"),
    POLYGON("Polygon"),
    MULTIPOLYGON("MultiPolygon");

    @Getter private final String name;

    public static ElementGeometryType from(String name) {
        switch(name.toLowerCase()) {
            case "point": return POINT;
            case "linestring": return LINESTRING;
            case "multilinestring": return MULTILINESTRING;
            case "polygon": return POLYGON;
            case "multipolygon": return MULTIPOLYGON;
            case "null": return NULL;
            default: return null;
        }
    }
}
