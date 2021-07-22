package com.mndk.kvm2m.core.vmap;

import com.mndk.kvm2m.core.db.common.TableColumns;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vmap.elem.VMapElement;
import com.mndk.kvm2m.core.vmap.elem.VMapLayer;
import com.mndk.kvm2m.core.vmap.elem.line.VMapContour;
import com.mndk.kvm2m.core.vmap.elem.line.VMapLineString;
import com.mndk.kvm2m.core.vmap.elem.line.VMapWall;
import com.mndk.kvm2m.core.vmap.elem.point.VMapElevationPoint;
import com.mndk.kvm2m.core.vmap.elem.point.VMapPoint;
import com.mndk.kvm2m.core.vmap.elem.poly.VMapBuilding;
import com.mndk.kvm2m.core.vmap.elem.poly.VMapPolygon;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

public class VMapPayload {


    public static VMapReaderResult combineVMapPayloads(
            Geometry geometryPayload,
            Data dataPayload,
            Map<String, String> options
    ) throws Exception {

        VMapReaderResult result = new VMapReaderResult();

        for(Map.Entry<Long, Geometry.Record<?>> entry : geometryPayload.entrySet()) {
            long id = entry.getKey();
            Geometry.Record<?> geometryRecord = entry.getValue();
            Data.Record dataRecord = dataPayload.get(id);

            if(dataRecord == null) continue;

            Object[] dataRow = dataRecord.getDataRow();

            VMapElementGeomType geometryType = geometryRecord.getType();
            VMapElementDataType dataType = dataRecord.getType();

            VMapLayer layer = result.getLayer(dataType);
            if(layer == null) {
                TableColumns columns = dataType.getColumns();
                String[] stringColumns = new String[columns.getLength()];
                for(int i = 0; i < stringColumns.length; ++i) {
                    stringColumns[i] = columns.get(i).getCategoryName();
                }
                result.addLayer(layer = new VMapLayer(dataType, stringColumns));
            }

            VMapElement element;
            Vector2DH[][] vertexList;

            switch(geometryType) {
                case POINT:
                    Vector2DH point = (Vector2DH) geometryRecord.getGeometryData();
                    if(dataType == VMapElementDataType.표고점) {
                        element = new VMapElevationPoint(layer, point, dataRow);
                    }
                    else {
                        element = new VMapPoint(layer, point, dataRow);
                    }
                    break;
                case LINESTRING:
                    vertexList = (Vector2DH[][]) geometryRecord.getGeometryData();
                    if(layer.getType() == VMapElementDataType.등고선) {
                        element = new VMapContour(layer, vertexList[0], dataRow);
                    }
                    else if(layer.getType() == VMapElementDataType.옹벽) {
                        element = new VMapWall(layer, vertexList, dataRow, false);
                    }
                    else {
                        element = new VMapLineString(layer, vertexList, dataRow, false);
                    }
                    break;
                case POLYGON:
                    vertexList = (Vector2DH[][]) geometryRecord.getGeometryData();

                    if(layer.getType() == VMapElementDataType.건물) {
                        if(options.containsKey("gen-building-shells")) {
                            element = new VMapBuilding(layer, vertexList, dataRow);
                        } else {
                            element = new VMapLineString(layer, vertexList, dataRow, false);
                        }
                    }
                    else {
                        element = new VMapPolygon(layer, vertexList, dataRow, true);
                    }
                    break;
                default: continue;
            }

            layer.add(element);

        }

        return result;

    }

    public static class Data extends HashMap<Long, Data.Record> {

        @RequiredArgsConstructor
        @Getter
        @ToString
        public static class Record {
            private final VMapElementDataType type;
            private final Object[] dataRow;
        }
    }

    public static class Geometry extends HashMap<Long, Geometry.Record<?>> {

        @RequiredArgsConstructor
        @Getter
        @ToString
        public static class Record<T> {
            private final VMapElementGeomType type;
            private final T geometryData;
        }
    }
}
