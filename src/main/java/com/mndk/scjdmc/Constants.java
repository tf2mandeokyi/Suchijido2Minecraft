package com.mndk.scjdmc;

import com.mndk.scjdmc.util.StackedThrowables;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.nio.charset.Charset;

public class Constants {

    public static final CoordinateReferenceSystem EPSG4326;
    public static final CoordinateReferenceSystem CRS84;

    public static final Charset CP949 = Charset.forName("CP949");

    public static final double POLYGON_BUFFER_EPSILON = 0.00000005;

    public static final String COASTLINE_GEOMETRY_FILE_NAME = "coastline_geometry.json";

    public static final String GEOMETRY_PROPERTY_NAME = "geometry";

    public static final GeometryFactory GEOMETRY_FACTORY = JTSFactoryFinder.getGeometryFactory();

    public static final GeometryJSON GEOMETRY_JSON = new GeometryJSON(8);
    public static final FeatureJSON FEATURE_JSON = new FeatureJSON(GEOMETRY_JSON);

    public static final StackedThrowables STACKED_THROWABLES = new StackedThrowables();

    static {
        try {
            EPSG4326 = CRS.decode("epsg:4326");
            CRS84 = CRS.decode("crs:84");
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        }
    }
}
