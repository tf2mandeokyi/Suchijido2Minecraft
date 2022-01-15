package com.mndk.scjdmc.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mndk.scjdmc.util.function.FeatureFilter;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FeatureGeometryUtils {


    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private static final Logger LOGGER = LogManager.getLogger();


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


    public static List<Geometry> extractGeometryList(SimpleFeatureCollection featureCollection) {
        return extractGeometryList(featureCollection, f -> true);
    }


    public static List<Geometry> extractGeometryList(SimpleFeatureCollection featureCollection, FeatureFilter featureFilter) {
        SimpleFeatureIterator featureIterator = featureCollection.features();
        List<Geometry> result = new ArrayList<>();
        while(featureIterator.hasNext()) {
            SimpleFeature f = featureIterator.next();
            if(!featureFilter.apply(f)) continue;
            result.add((Geometry) f.getDefaultGeometry());
        }
        return result;
    }


    public static SimpleFeatureCollection subtractPolygonsToPolygonCollection(
            SimpleFeatureType victimType, SimpleFeatureCollection victim, List<Geometry> subtract, Function<Integer, String> idFunction
    ) {
        @AllArgsConstructor class Entry {
            final SimpleFeature origin;
            Geometry geometry;
            final SimpleFeatureType featureType;
        }

        List<Geometry> newSubtractList = subtract.stream()
                .filter(g -> g instanceof Polygon || g instanceof MultiPolygon)
                .map(g -> g.buffer(Constants.POLYGON_BUFFER_EPSILON))
                .collect(Collectors.toList());

        Map<Integer, List<Integer>> intersectingEntries = new HashMap<>();
        List<Entry> entryList = new ArrayList<>();

        // Calculate intersections
        SimpleFeatureIterator victimFeatureIterator = victim.features();
        while (victimFeatureIterator.hasNext()) {
            SimpleFeature feature = victimFeatureIterator.next();
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            SimpleFeatureType featureType = feature.getType();
            int index;

            // Skip if geometry is not polygon
            if(!(geometry instanceof Polygon) && !(geometry instanceof MultiPolygon)) {
                entryList.add(new Entry(feature, geometry, featureType));
                continue;
            }

            // Check intersections
            List<Polygon> polygons = extractPolygonsFromGeometry(geometry);
            for(Polygon polygon : polygons) {
                index = entryList.size();
                entryList.add(new Entry(feature, polygon, featureType));

                for (int i = 0; i < newSubtractList.size(); i++) {
                    if (geometry.intersects(newSubtractList.get(i))) {
                        intersectingEntries.computeIfAbsent(i, k -> new ArrayList<>()).add(index);
                    }
                }
            }
        }

        // Calculate subtractions
        for (Map.Entry<Integer, List<Integer>> mapEntry : intersectingEntries.entrySet()) {
            int subtractIndex = mapEntry.getKey();
            List<Integer> intersectingIndexes = mapEntry.getValue();

            for (Integer index : intersectingIndexes) {
                Geometry subtractGeometry = newSubtractList.get(subtractIndex);
                if (subtractGeometry == null) {
                    break;
                }

                Entry entry = entryList.get(index);
                if (entry.geometry == null) {
                    continue;
                }

                Geometry newEntryGeometry = getPolygonDifference(entry.geometry, subtractGeometry);
                Geometry newSubtractGeometry = getPolygonDifference(subtractGeometry, entry.geometry);

                entry.geometry = newEntryGeometry.isEmpty() ? null : newEntryGeometry;
                newSubtractList.set(subtractIndex, newSubtractGeometry.isEmpty() ? null : newSubtractGeometry);

            }
        }

        int i = 1;
        ListFeatureCollection result = new ListFeatureCollection(victimType);
        for(Entry entry : entryList) {
            if(entry.geometry != null) {
                result.add(replaceFeatureGeometry(
                        entry.origin, entry.geometry, idFunction == null ? null : idFunction.apply(i++)
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


    public static Geometry polygonListToGeometry(List<Polygon> polygonList) {
        if(polygonList.size() == 1) return polygonList.get(0);
        return new MultiPolygon(polygonList.toArray(new Polygon[0]), GEOMETRY_FACTORY);
    }


    private static Geometry getPolygonDifference(Geometry origin, final Geometry diff) {
        List<Polygon> result = new ArrayList<>();
        if (origin instanceof GeometryCollection || diff instanceof GeometryCollection) {
            for (int j = 0; j < diff.getNumGeometries(); j++) {
                Geometry subtractGeometry = diff.getGeometryN(j);
                if (!(subtractGeometry instanceof Polygon)) continue;

                result.clear();
                for (int i = 0; i < origin.getNumGeometries(); i++) {
                    Geometry originGeometry = origin.getGeometryN(i);
                    if (!(originGeometry instanceof Polygon)) continue;

                    try {
                        originGeometry.difference(subtractGeometry).apply((GeometryFilter) difference -> {
                            if (difference instanceof Polygon && !difference.isEmpty()) {
                                result.add((Polygon) difference);
                            }
                        });
                    } catch(TopologyException e) {
                        LOGGER.warn(e);
                        result.add((Polygon) originGeometry);
                    }
                }
                origin = polygonListToGeometry(result);
            }
        }
        else if(origin instanceof Polygon && diff instanceof Polygon) {
            try {
                origin.difference(diff).apply((GeometryFilter) difference -> {
                    if (difference instanceof Polygon) {
                        result.add((Polygon) difference);
                    }
                });
            } catch(TopologyException e) {
                LOGGER.warn(e);
                result.add((Polygon) origin);
            }
        }

        return polygonListToGeometry(result);
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
