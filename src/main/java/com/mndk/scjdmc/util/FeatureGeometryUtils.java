package com.mndk.scjdmc.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.*;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class FeatureGeometryUtils {


    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();


    public static Polygon lineStringToOuterEdgeOnlyPolygon(Geometry lineStringGeometry) {

        // Validation
        if(!(lineStringGeometry instanceof LineString) && !(lineStringGeometry instanceof MultiLineString)) {
            return null;
        }

        Coordinate[] coordinates = lineStringGeometry.getCoordinates();

        // Check lineString if closed
        if(!coordinates[0].equals2D(coordinates[coordinates.length - 1])) {
            coordinates = Arrays.copyOf(coordinates, coordinates.length + 1);
            coordinates[coordinates.length - 1] = coordinates[0];
        }

        LinearRing linearRing = new LinearRing(
                GEOMETRY_FACTORY.getCoordinateSequenceFactory().create(coordinates),
                GEOMETRY_FACTORY
        );

        return new Polygon(linearRing, new LinearRing[0], GEOMETRY_FACTORY);
    }


    public static SimpleFeatureCollection getFeatureCollectionGeometryDifference(
            SimpleFeatureType victimType, SimpleFeatureCollection victim, SimpleFeatureCollection other
    ) {
        SimpleFeatureIterator victimFeatureIterator = victim.features();
        ListFeatureCollection listFeatureCollection = new ListFeatureCollection(victimType);

        while (victimFeatureIterator.hasNext()) {
            SimpleFeature feature = getFeatureCollectionGeometryDifference(victimFeatureIterator.next(), other);
            if(feature == null) continue;
            listFeatureCollection.add(feature);
        }
        return listFeatureCollection;
    }


    public static SimpleFeatureCollection subtractFeatureCollectionToPolygonCollection(
            SimpleFeatureType victimType, SimpleFeatureCollection victim, SimpleFeatureCollection subtractCollection,
            Function<SimpleFeature, Boolean> subtractFilter, Function<Integer, String> idFunction
    ) {
        SimpleFeatureIterator subtractIterator = subtractCollection.features();
        while(subtractIterator.hasNext()) {
            SimpleFeature subtract = subtractIterator.next();
            if(subtractFilter != null && !subtractFilter.apply(subtract)) continue;

            Geometry subtractGeometry = (Geometry) subtract.getDefaultGeometry();
            victim = subtractGeometryToPolygonCollection(
                    victimType,
                    victim, subtractGeometry.buffer(Constants.POLYGON_BUFFER_EPSILON),
                    idFunction
            );
        }
        return victim;
    }


    public static SimpleFeatureCollection subtractGeometryToPolygonCollection(
            SimpleFeatureType victimType, SimpleFeatureCollection victim, Geometry subtract, Function<Integer, String> idFunction
    ) {
        List<Polygon> polygons = extractPolygonsFromGeometry(subtract);
        for(Polygon polygon : polygons) {
            victim = subtractSinglePolygonToPolygonCollection(victimType, victim, polygon, idFunction);
        }
        return victim;
    }


    public static SimpleFeatureCollection subtractSinglePolygonToPolygonCollection(
            SimpleFeatureType victimType, SimpleFeatureCollection victim, Polygon subtract, Function<Integer, String> idFunction
    ) {
        @AllArgsConstructor class Entry {
            final SimpleFeature origin;
            final Polygon polygon;
            final SimpleFeatureType featureType;
            final boolean computeIntersection;
            int stack;
        }

        List<Entry> list = new ArrayList<>();
        SimpleFeatureIterator victimFeatureIterator = victim.features();
        int size;

        while (victimFeatureIterator.hasNext()) {
            SimpleFeature feature = victimFeatureIterator.next();
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            SimpleFeatureType featureType = feature.getType();
            boolean intersects = geometry.intersects(subtract);
            if(intersects) {
                size = list.size();
                for (int i = 0; i < size; i++) {
                    Entry entry = list.get(i);
                    List<Polygon> polygons = getPolygonIntersection(entry.polygon, geometry);
                    for(Polygon polygon1 : polygons) {
                        list.add(new Entry(entry.origin, polygon1, entry.featureType, true, entry.stack + 1));
                    }
                }
            }
            List<Polygon> polygons = extractPolygonsFromGeometry(geometry);
            for(Polygon polygon : polygons) {
                list.add(new Entry(feature, polygon, featureType, intersects, 1));
            }
        }
        size = list.size();
        for(int i = 0; i < size; i++) {
            Entry entry = list.get(i);
            if(entry.computeIntersection && entry.polygon.intersects(subtract)) {
                List<Polygon> diff = getPolygonDifference(entry.polygon, subtract);
                for(Polygon polygon1 : diff) {
                    list.add(new Entry(entry.origin, polygon1, entry.featureType, true, entry.stack));
                }
                entry.stack--;
            }
        }
        int i = 1;
        ListFeatureCollection result = new ListFeatureCollection(victimType);
        for(Entry entry : list) {
            if(entry.stack > 0 && !entry.polygon.isEmpty()) {
                result.add(replaceFeatureGeometry(
                        entry.origin, entry.polygon, idFunction == null ? null : idFunction.apply(i++)
                ));
            }
        }
        return result;
    }


    public static List<Polygon> extractPolygonsFromGeometry(Geometry g) {
        if(g instanceof Polygon) {
            if(!g.isEmpty()) return Collections.singletonList((Polygon) g);
        }
        else if(g instanceof MultiPolygon) {
            return multiPolygonToList((MultiPolygon) g);
        }
        else if(g instanceof GeometryCollection) {
            List<Polygon> polygons = new ArrayList<>();
            GeometryCollection geometryCollection = (GeometryCollection) g;
            for(int i = 0; i < geometryCollection.getNumGeometries(); i++) {
                Geometry tempGeometry = geometryCollection.getGeometryN(i);
                if(tempGeometry instanceof Polygon) {
                    if(!tempGeometry.isEmpty()) polygons.add((Polygon) tempGeometry);
                }
                else if(tempGeometry instanceof MultiPolygon) {
                    polygons.addAll(multiPolygonToList((MultiPolygon) tempGeometry));
                }
            }
            return polygons;
        }
        return Collections.emptyList();
    }


    public static List<Polygon> multiPolygonToList(MultiPolygon mp) {
        List<Polygon> result = new ArrayList<>(mp.getNumGeometries());
        for(int i = 0; i < mp.getNumGeometries(); i++) {
            Polygon temp = (Polygon) mp.getGeometryN(i);
            if(!temp.isEmpty()) result.add(temp);
        }
        return result;
    }


    private static List<Polygon> getPolygonIntersection(Geometry g1, Geometry g2) {
        Geometry intersection = g1.intersection(g2);
        return extractPolygonsFromGeometry(intersection);
    }


    private static List<Polygon> getPolygonDifference(Geometry origin, final Geometry diff) {
        List<Polygon> result = new ArrayList<>();
        origin.apply((GeometryFilter) g -> diff.apply((GeometryFilter) g1 -> origin.difference(diff).apply((GeometryFilter) difference -> {
            if(difference instanceof Polygon) {
                result.add((Polygon) difference);
            }
        })));
        return result;
    }


    public static SimpleFeature getFeatureCollectionGeometryDifference(
            SimpleFeature victim, SimpleFeatureCollection other
    ) {
        Geometry geometry = ((Geometry) victim.getDefaultGeometry());
        if(geometry == null) return null;
        geometry = getFeatureCollectionGeometryDifference(geometry, other);
        return geometry == null ? null : replaceFeatureGeometry(victim, geometry, null);
    }


    public static Geometry getFeatureCollectionGeometryDifference(
            Geometry victim, SimpleFeatureCollection other
    ) {
        SimpleFeatureIterator featureIterator = other.features();
        while (featureIterator.hasNext()) {
            Geometry geometry = (Geometry) featureIterator.next().getDefaultGeometry();
            if(geometry == null) continue;
            victim = victim.difference(geometry.buffer(Constants.POLYGON_BUFFER_EPSILON));
        }
        return victim.isEmpty() ? null : victim;
    }


    public static SimpleFeature replaceFeatureGeometry(SimpleFeature feature, Geometry newGeometry, String newId) {
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(feature.getFeatureType());
        featureBuilder.set(Constants.GEOMETRY_PROPERTY_NAME, newGeometry);
        for(Property property : feature.getProperties()) {
            if(property.getName().toString().equals(Constants.GEOMETRY_PROPERTY_NAME)) continue;
            featureBuilder.set(property.getName(), property.getValue());
        }
        return featureBuilder.buildFeature(newId == null ? feature.getID() : newId);
    }


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
