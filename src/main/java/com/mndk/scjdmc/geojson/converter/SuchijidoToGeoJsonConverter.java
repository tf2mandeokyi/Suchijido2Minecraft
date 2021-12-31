package com.mndk.scjdmc.geojson.converter;

import com.mndk.scjdmc.util.function.LayerFilterFunction;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.File;

public abstract class SuchijidoToGeoJsonConverter {

    public static final Logger LOGGER = LogManager.getLogger();

    protected static final CoordinateReferenceSystem EPSG4326;
    protected static final CoordinateReferenceSystem CRS84;

    protected final FeatureJSON featureJSON;
    @Setter protected LayerFilterFunction layerFilter;

    public SuchijidoToGeoJsonConverter(FeatureJSON featureJSON) {
        this.featureJSON = featureJSON;
        this.layerFilter = LayerFilterFunction.DEFAULT_FILTER;
    }

    public abstract void toFeatureJSON(File source, File destination, String charset) throws Exception;

    static {
        try {
            EPSG4326 = CRS.decode("epsg:4326");
            CRS84 = CRS.decode("crs:84");
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        }
    }

}
