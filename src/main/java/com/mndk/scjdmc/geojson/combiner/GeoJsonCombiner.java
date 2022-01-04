package com.mndk.scjdmc.geojson.combiner;

import com.mndk.scjdmc.util.function.ConversionCompleteFunction;
import com.mndk.scjdmc.util.function.LayerFilterFunction;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public abstract class GeoJsonCombiner {

    protected static final Logger LOGGER = LogManager.getLogger();

    @Setter protected LayerFilterFunction layerFilter;
    @Setter protected ConversionCompleteFunction onConversionCompleteFunction;

    public GeoJsonCombiner() {
        this.layerFilter = LayerFilterFunction.DEFAULT_FILTER;
        this.onConversionCompleteFunction = (s, b) -> {};
    }

    public abstract void combine(File source, File destination);
}
