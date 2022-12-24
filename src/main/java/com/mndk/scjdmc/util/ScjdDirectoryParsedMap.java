package com.mndk.scjdmc.util;

import com.mndk.scjdmc.column.LayerDataType;
import com.mndk.scjdmc.util.file.ScjdFileInformation;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Geometry;

import java.util.*;

public class ScjdDirectoryParsedMap<T> {

    @Getter private final ScjdFileInformation fileInformation;
    private final Map<LayerDataType, List<T>> map;
    @Getter @Setter private Geometry coastlineGeometry;

    public ScjdDirectoryParsedMap(ScjdFileInformation fileInformation) {
        this.fileInformation = fileInformation;
        this.map = new HashMap<>();
    }

    public void set(LayerDataType layerDataType, List<T> list) {
        this.map.put(layerDataType, list);
    }

    public void put(LayerDataType layerDataType, T element) {
        if(!this.map.containsKey(layerDataType)) {
            map.put(layerDataType, new ArrayList<>());
        }
        map.get(layerDataType).add(element);
    }

    public List<T> get(LayerDataType layerDataType) {
        return this.map.get(layerDataType);
    }

    public Set<Map.Entry<LayerDataType, List<T>>> entrySet() {
        return this.map.entrySet();
    }

}
