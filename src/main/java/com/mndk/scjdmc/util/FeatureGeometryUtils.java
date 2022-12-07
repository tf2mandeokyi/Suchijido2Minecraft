package com.mndk.scjdmc.util;

import com.mndk.scjdmc.Constants;
import com.mndk.scjdmc.util.function.FeatureFilter;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.locationtech.jts.geom.*;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FeatureGeometryUtils {


    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private static final Logger LOGGER = LogManager.getLogger();


    /**
     * Converts LineString into Polygon, using LineString's coordinates as its outer edge.
     *
     * @param lineStringGeometry LineString that is to be converted
     * @return Polygon
     */
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


    /**
     * Extracts feature's geometry list from FeatureCollection, without any filters
     *
     * @param featureCollection FeatureCollection
     * @return List of geometries
     */
    public static List<Geometry> extractGeometryAsList(SimpleFeatureCollection featureCollection) {
        return extractGeometryAsList(featureCollection, f -> true);
    }


    /**
     * Extracts feature's geometries from FeatureCollection
     *
     * @param featureCollection FeatureCollection, that can be null
     * @param featureFilter filter to be applied into features
     * @return List of geometries
     */
    public static List<Geometry> extractGeometryAsList(
            SimpleFeatureCollection featureCollection, FeatureFilter featureFilter
    ) {
        if(featureCollection == null) return Collections.emptyList();

        SimpleFeatureIterator featureIterator = featureCollection.features();
        List<Geometry> result = new ArrayList<>();
        while(featureIterator.hasNext()) {
            SimpleFeature f = featureIterator.next();
            if(!featureFilter.apply(f)) continue;
            result.add((Geometry) f.getDefaultGeometry());
        }
        return result;
    }


    /**
     * Applies Geometry#difference() to every feature in a featureCollection, while featureCollection
     * being a victim and list of geometries being subtractions.
     * <br>
     * This method also buffers every geometry in a list before the subtraction, so that no garbage
     * is left while cutting the victim.
     *
     * @param victimType Type of victim
     * @param victim FeatureCollection
     * @param other list of geometries
     * @return Geometry#difference() applied feature collection
     */
    public static SimpleFeatureCollection getFeatureCollectionGeometryDifference(
            SimpleFeatureType victimType, SimpleFeatureCollection victim, List<Geometry> other
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


    /**
     * Applies Geometry#difference() to a feature, while list of geometries being subtractions
     * <br>
     * This method also buffers every geometry in a list before the subtraction, so that no garbage
     * is left while cutting the victim.
     *
     * @param victim Feature
     * @param other list of geometries
     * @return Geometry#difference() applied feature
     */
    public static SimpleFeature getFeatureCollectionGeometryDifference(
            SimpleFeature victim, List<Geometry> other
    ) {
        Geometry geometry = ((Geometry) victim.getDefaultGeometry());
        if(geometry == null) return null;
        geometry = getFeatureCollectionGeometryDifference(geometry, other);
        return geometry == null ? null : replaceFeatureGeometry(victim, geometry, null);
    }


    /**
     * Applies Geometry#difference() to a geometry, while list of other geometries being subtractions
     * <br>
     * This method also buffers every geometry in a list before the subtraction, so that no garbage
     * is left while cutting the victim.
     *
     * @param victim Geometry to be cut
     * @param other list of geometries
     * @return Cut geometry
     */
    public static Geometry getFeatureCollectionGeometryDifference(
            Geometry victim, List<Geometry> other
    ) {
        for (Geometry geometry : other) {
            if(geometry == null) continue;
            victim = getGeometryDifference(victim, geometry.buffer(Constants.POLYGON_BUFFER_EPSILON), false);
        }
        return victim.isEmpty() ? null : victim;
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

                Geometry newEntryGeometry = getGeometryDifference(entry.geometry, subtractGeometry, true);
                Geometry newSubtractGeometry = getGeometryDifference(subtractGeometry, entry.geometry, true);

                entry.geometry = newEntryGeometry.isEmpty() ? null : newEntryGeometry;
                newSubtractList.set(subtractIndex, newSubtractGeometry.isEmpty() ? null : newSubtractGeometry);
            }
        }

        int i = 1;
        ListFeatureCollection result = new ListFeatureCollection(victimType);
        for(Entry entry : entryList) {
            if(entry.geometry != null) {
                if(entry.geometry instanceof MultiPolygon) {
                    // Splitting multipolygon because terra++ doesn't seem to like it
                    for(int j = 0; j < entry.geometry.getNumGeometries(); j++) {
                        result.add(replaceFeatureGeometry(
                                entry.origin, entry.geometry.getGeometryN(j), idFunction == null ? null : idFunction.apply(i++)
                        ));
                    }
                }
                else {
                    result.add(replaceFeatureGeometry(
                            entry.origin, entry.geometry, idFunction == null ? null : idFunction.apply(i++)
                    ));
                }
            }
        }
        return result;
    }


    /**
     * Extracts polygons as list from geometry
     *
     * @param g Geometry
     * @return List of polygons; empty list if LineString, Point, MultiLineString, etc.
     */
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


    /**
     * Converts MultiPolygon into List&lt;Polygon&gt;
     * @param mp MultiPolygon
     * @return Converted list
     */
    public static List<Polygon> multiPolygonToList(MultiPolygon mp) {
        List<Polygon> result = new ArrayList<>(mp.getNumGeometries());
        for(int i = 0; i < mp.getNumGeometries(); i++) {
            Polygon temp = (Polygon) mp.getGeometryN(i);
            if(!temp.isEmpty()) result.add(temp);
        }
        return result;
    }


    /**
     * Subtracts geometry "origin" to geometry "diff" - <br>
     * "diff" being scissors and "origin" being a paper
     *
     * @param origin Paper
     * @param diff Scissors
     * @param polygonOnly Whether to only use polygons as scissors
     * @return Cut geometry
     */
    private static Geometry getGeometryDifference(Geometry origin, final Geometry diff, boolean polygonOnly) {
        List<Polygon> result = new ArrayList<>();
        if (origin instanceof GeometryCollection || diff instanceof GeometryCollection) {
            for (int j = 0; j < diff.getNumGeometries(); j++) {
                Geometry subtractGeometry = diff.getGeometryN(j);
                if (polygonOnly && !(subtractGeometry instanceof Polygon)) continue;

                result.clear();
                for (int i = 0; i < origin.getNumGeometries(); i++) {
                    Geometry originGeometry = origin.getGeometryN(i);
                    if (polygonOnly && !(originGeometry instanceof Polygon)) continue;

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


    public static Geometry polygonListToGeometry(List<Polygon> polygonList) {
        if(polygonList.size() == 1) return polygonList.get(0);
        return new MultiPolygon(polygonList.toArray(new Polygon[0]), GEOMETRY_FACTORY);
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

}
