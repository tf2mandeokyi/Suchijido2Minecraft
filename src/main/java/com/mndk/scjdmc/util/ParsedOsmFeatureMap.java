package com.mndk.scjdmc.util;

import com.mndk.scjdmc.scjd.LayerDataType;
import lombok.Getter;
import org.geotools.data.simple.SimpleFeatureCollection;

import java.util.HashMap;

public class ParsedOsmFeatureMap extends HashMap<LayerDataType, SimpleFeatureCollection> {

    @Getter private final String name;
    @Getter private final ScjdParsedType type;

    public ParsedOsmFeatureMap(String name, ScjdParsedType type) {
        this.name = name;
        this.type = type;
    }

}
