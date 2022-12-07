package com.mndk.scjdmc;

import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class Constants {

    public static final CoordinateReferenceSystem EPSG4326;
    public static final CoordinateReferenceSystem CRS84;

    public static final double POLYGON_BUFFER_EPSILON = 0.000001;

    public static final String GEOMETRY_PROPERTY_NAME = "geometry";

    public static final FeatureJSON FEATURE_JSON = new FeatureJSON(new GeometryJSON(7));

    static {
        try {
            EPSG4326 = CRS.decode("epsg:4326");
            CRS84 = CRS.decode("crs:84");
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        }
    }
}
