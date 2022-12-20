package com.mndk.scjdmc.util.function;

import com.mndk.scjdmc.scjd.LayerDataType;
import org.geotools.data.simple.SimpleFeatureCollection;

import java.io.IOException;

public interface ScjdFeatureCollectionFunction<T> {
    T apply(SimpleFeatureCollection p1, LayerDataType p2) throws IOException;
}
