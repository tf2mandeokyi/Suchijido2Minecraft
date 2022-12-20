package com.mndk.scjdmc.geojson.jsonpacker;

import com.mndk.scjdmc.geojson.converter.ShapefileConversionResult;
import com.mndk.scjdmc.util.function.LayerFilterFunction;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.geojson.feature.FeatureJSON;

import java.io.IOException;
import java.io.Writer;
import java.util.function.Function;

@Deprecated
public abstract class ScjdJsonPacker {

    protected static final Logger LOGGER = LogManager.getLogger();

    protected final FeatureJSON featureJSON;
    @Setter protected LayerFilterFunction layerFilter;
    @Setter protected Function<String, Boolean> indexCoastlineFilter;

    public ScjdJsonPacker(FeatureJSON featureJSON) {
        this.featureJSON = featureJSON;
        this.layerFilter = LayerFilterFunction.DEFAULT_FILTER;
        this.indexCoastlineFilter = index -> true;
    }

    public abstract void pack(ShapefileConversionResult conversion, Writer writer) throws IOException;
}
