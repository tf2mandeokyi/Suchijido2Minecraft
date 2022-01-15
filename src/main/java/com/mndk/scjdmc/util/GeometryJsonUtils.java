package com.mndk.scjdmc.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.geometry.BoundingBox;

public class GeometryJsonUtils {
    public static JsonObject validateJsonGeometry(JsonObject geometry) {
        String geometryType = geometry.get("type").getAsString();
        JsonArray newGeometryArray = validateJsonCoordinates(
                geometry.getAsJsonArray("coordinates"),
                getJsonCoordinatesDepth(geometryType),
                getMinimumJsonCoordinatesArrayLength(geometryType)
        );
        if(geometryType.startsWith("Multi") && newGeometryArray.size() == 1) {
            // Exclude "multi"
            geometryType = geometryType.substring(5);
            newGeometryArray = newGeometryArray.get(0).getAsJsonArray();
        }
        if(newGeometryArray.size() == 0) {
            return null;
        }
        JsonObject newGeometryObject = new JsonObject();
        newGeometryObject.addProperty("type", geometryType);
        newGeometryObject.add("coordinates", newGeometryArray);
        return newGeometryObject;
    }

    public static JsonArray validateJsonCoordinates(JsonArray coordinates, int depth, int minimumLength) {
        if(depth == 0) return coordinates;

        JsonArray newGeometry = new JsonArray();
        if(depth == 1) {
            if (coordinates.size() == 0) return coordinates;

            JsonArray point = coordinates.get(0).getAsJsonArray();
            while(point.size() > 2) point.remove(2);
            newGeometry.add(point);

            double prevLat = point.get(0).getAsDouble(), prevLon = point.get(1).getAsDouble(), lat, lon;
            for (int i = 1; i < coordinates.size(); i++) {
                // Duplicate point detection
                point = coordinates.get(i).getAsJsonArray();
                while(point.size() > 2) point.remove(2);
                lat = point.get(0).getAsDouble();
                lon = point.get(1).getAsDouble();
                if (prevLat == lat && prevLon == lon) continue;
                newGeometry.add(point);
                prevLat = lat;
                prevLon = lon;
            }

            if(newGeometry.size() < minimumLength) return new JsonArray();
        }
        else {
            for (JsonElement element : coordinates) {
                JsonArray validation = validateJsonCoordinates(element.getAsJsonArray(), depth - 1, minimumLength);
                if(validation.size() != 0) newGeometry.add(validation);
            }
        }

        return newGeometry;
    }

    public static BoundingBox getJsonGeometryBoundingBox(JsonObject geometry) {
        return getJsonCoordinatesBoundingBox(
                geometry.getAsJsonArray("coordinates"),
                getJsonCoordinatesDepth(geometry.get("type").getAsString())
        );
    }

    public static BoundingBox getJsonCoordinatesBoundingBox(JsonArray coordinates, int depth) {
        if(depth == 0) {
            double lon = coordinates.get(0).getAsDouble(), lat = coordinates.get(1).getAsDouble();
            return new ReferencedEnvelope(lon, lon, lat, lat, null);
        }
        else {
            BoundingBox result = null;
            for(JsonElement element : coordinates) {
                JsonArray array = element.getAsJsonArray();
                BoundingBox bbox = getJsonCoordinatesBoundingBox(array, depth - 1);
                if(result == null) result = bbox;
                else result.include(bbox);
            }
            return result;
        }
    }

    private static int getJsonCoordinatesDepth(String geometryType) {
        switch(geometryType) {
            case "Point": return 0;
            case "LineString": case "MultiPoint": return 1;
            case "Polygon": case "MultiLineString": return 2;
            case "MultiPolygon": return 3;
            default: throw new IllegalArgumentException("Illegal type: " + geometryType);
        }
    }

    private static int getMinimumJsonCoordinatesArrayLength(String geometryType) {
        switch(geometryType) {
            case "Point": case "MultiPoint": return 0;
            case "LineString": case "MultiLineString": return 2;
            case "Polygon": case "MultiPolygon": return 4;
            default: throw new IllegalArgumentException("Illegal type: " + geometryType);
        }
    }
}
