package com.mndk.scjdmc.util;

import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.locationtech.jts.geom.*;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.util.Arrays;

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


    public static SimpleFeatureCollection featureCollectionGeometryDifference(
            SimpleFeatureType victimType, SimpleFeatureCollection victim, SimpleFeatureCollection other
    ) {
        SimpleFeatureIterator victimFeatureIterator = victim.features();
        ListFeatureCollection listFeatureCollection = new ListFeatureCollection(victimType);

        while (victimFeatureIterator.hasNext()) {
            SimpleFeature feature = feature_featureCollectionGeometryDifference(
                    victimFeatureIterator.next(), other
            );
            if(feature == null) continue;
            listFeatureCollection.add(feature);
        }
        return listFeatureCollection;
    }


    public static SimpleFeature feature_featureCollectionGeometryDifference(
            SimpleFeature victim, SimpleFeatureCollection other
    ) {
        Geometry geometry = ((Geometry) victim.getDefaultGeometry());
        geometry = geometry_featureCollectionGeometryDifference(geometry, other);
        return geometry == null ? null : replaceGeometry(victim, geometry);
    }


    public static Geometry geometry_featureCollectionGeometryDifference(
            Geometry geometry, SimpleFeatureCollection other
    ) {
        SimpleFeatureIterator tunnelFeatureIterator = other.features();
        while (tunnelFeatureIterator.hasNext()) {
            Geometry tunnelGeometry = (Geometry) tunnelFeatureIterator.next().getDefaultGeometry();
            geometry = geometry.difference(tunnelGeometry.buffer(Constants.POLYGON_BUFFER_EPSILON));
        }

        return geometry.isEmpty() ? null : geometry;
    }


    public static SimpleFeature replaceGeometry(SimpleFeature feature, Geometry newGeometry) {
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(feature.getFeatureType());
        featureBuilder.set(Constants.GEOMETRY_PROPERTY_NAME, newGeometry);
        for(Property property : feature.getProperties()) {
            if(property.getName().toString().equals(Constants.GEOMETRY_PROPERTY_NAME)) continue;
            featureBuilder.set(property.getName(), property.getValue());
        }
        return featureBuilder.buildFeature(feature.getID());
    }
}
