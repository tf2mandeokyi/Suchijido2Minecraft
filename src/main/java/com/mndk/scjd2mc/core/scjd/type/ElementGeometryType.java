package com.mndk.scjd2mc.core.scjd.type;

public enum ElementGeometryType {
    NULL, POINT, LINESTRING, POLYGON;
    public static ElementGeometryType from(String name) {
        switch(name.toLowerCase()) {
            case "point": return POINT;
            case "linestring": return LINESTRING;
            case "polygon": return POLYGON;
            case "null": return NULL;
            default: return null;
        }
    }
}
