package com.mndk.scjdmc.geojson.converter;

import com.mndk.scjdmc.scjd.LayerDataType;
import lombok.Getter;
import org.geotools.data.simple.SimpleFeatureCollection;

import java.util.HashMap;

@Deprecated
public class ShapefileConversionResult extends HashMap<LayerDataType, SimpleFeatureCollection> {

    @Getter
    private final String index;

    public ShapefileConversionResult(String index) {
        this.index = index;
    }

}
