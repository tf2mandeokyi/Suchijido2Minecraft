package com.mndk.scjdmc.geojson.jsonpacker;

import com.mndk.scjdmc.geojson.converter.ShapefileConversionResult;
import com.mndk.scjdmc.util.function.LayerFilterFunction;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.geojson.feature.FeatureJSON;

import java.io.IOException;
import java.io.Writer;

public abstract class JsonPacker {

    protected static final Logger LOGGER = LogManager.getLogger();

    protected final FeatureJSON featureJSON;
    @Setter protected LayerFilterFunction layerFilter;

    public JsonPacker(FeatureJSON featureJSON) {
        this.featureJSON = featureJSON;
        this.layerFilter = LayerFilterFunction.DEFAULT_FILTER;
    }

    public abstract void pack(ShapefileConversionResult result, Writer writer) throws IOException;
}
