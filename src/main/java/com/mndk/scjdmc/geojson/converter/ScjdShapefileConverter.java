package com.mndk.scjdmc.geojson.converter;

import com.mndk.scjdmc.util.function.LayerFilterFunction;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.File;

@Deprecated
public abstract class ScjdShapefileConverter<ReturnType> {

    protected static final Logger LOGGER = LogManager.getLogger();

    protected static final CoordinateReferenceSystem EPSG4326;
    protected static final CoordinateReferenceSystem CRS84;

    @Setter protected LayerFilterFunction layerFilter;

    public ScjdShapefileConverter() {
        this.layerFilter = LayerFilterFunction.DEFAULT_FILTER;
    }

    public abstract ReturnType convert(File source, String charset) throws Exception;

    static {
        try {
            EPSG4326 = CRS.decode("epsg:4326");
            CRS84 = CRS.decode("crs:84");
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        }
    }

}
