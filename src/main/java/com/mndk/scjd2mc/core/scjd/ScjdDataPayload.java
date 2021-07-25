package com.mndk.scjd2mc.core.scjd;

import com.mndk.scjd2mc.core.db.common.TableColumns;
import com.mndk.scjd2mc.core.util.math.Vector2DH;
import com.mndk.scjd2mc.core.scjd.elem.ScjdElement;
import com.mndk.scjd2mc.core.scjd.elem.ScjdLayer;
import com.mndk.scjd2mc.core.scjd.elem.line.ScjdContour;
import com.mndk.scjd2mc.core.scjd.elem.line.ScjdLineString;
import com.mndk.scjd2mc.core.scjd.elem.line.ScjdWall;
import com.mndk.scjd2mc.core.scjd.elem.point.ScjdElevationPoint;
import com.mndk.scjd2mc.core.scjd.elem.point.ScjdPoint;
import com.mndk.scjd2mc.core.scjd.elem.poly.ScjdBuilding;
import com.mndk.scjd2mc.core.scjd.elem.poly.ScjdPolygon;
import com.mndk.scjd2mc.core.scjd.type.ElementDataType;
import com.mndk.scjd2mc.core.scjd.type.ElementGeometryType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

public class ScjdDataPayload {


    public static ScjdReaderResult combineVMapPayloads(
            Geometry geometryPayload,
            Data dataPayload,
            Map<String, String> options
    ) throws Exception {

        ScjdReaderResult result = new ScjdReaderResult();

        for(Map.Entry<Long, Geometry.Record<?>> entry : geometryPayload.entrySet()) {
            long id = entry.getKey();
            Geometry.Record<?> geometryRecord = entry.getValue();
            Data.Record dataRecord = dataPayload.get(id);

            if(dataRecord == null) continue;

            Object[] dataRow = dataRecord.getDataRow();

            ElementGeometryType geometryType = geometryRecord.getType();
            ElementDataType dataType = dataRecord.getType();

            ScjdLayer layer = result.getLayer(dataType);
            if(layer == null) {
                TableColumns columns = dataType.getColumns();
                String[] stringColumns = new String[columns.getLength()];
                for(int i = 0; i < stringColumns.length; ++i) {
                    stringColumns[i] = columns.get(i).getName();
                }
                result.addLayer(layer = new ScjdLayer(dataType, stringColumns));
            }

            ScjdElement element;
            Vector2DH[][] vertexList;

            switch(geometryType) {
                case POINT:
                    Vector2DH point = (Vector2DH) geometryRecord.getGeometryData();
                    if(dataType == ElementDataType.표고점) {
                        element = new ScjdElevationPoint(layer, point, dataRow);
                    }
                    else {
                        element = new ScjdPoint(layer, Long.toString(id), point, dataRow);
                    }
                    break;
                case LINESTRING:
                    vertexList = (Vector2DH[][]) geometryRecord.getGeometryData();
                    if(layer.getType() == ElementDataType.등고선) {
                        element = new ScjdContour(layer, vertexList[0], dataRow);
                    }
                    else if(layer.getType() == ElementDataType.옹벽) {
                        element = new ScjdWall(layer, Long.toString(id), vertexList, dataRow, false);
                    }
                    else {
                        element = new ScjdLineString(layer, Long.toString(id), vertexList, dataRow, false);
                    }
                    break;
                case POLYGON:
                    vertexList = (Vector2DH[][]) geometryRecord.getGeometryData();

                    if(layer.getType() == ElementDataType.건물) {
                        if(options.containsKey("gen-building-shells")) {
                            element = new ScjdBuilding(layer, Long.toString(id), vertexList, dataRow);
                        } else {
                            element = new ScjdPolygon(layer, Long.toString(id), vertexList, dataRow, false);
                        }
                    }
                    else {
                        element = new ScjdPolygon(layer, Long.toString(id), vertexList, dataRow, true);
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
            private final ElementDataType type;
            private final Object[] dataRow;
        }
    }

    public static class Geometry extends HashMap<Long, Geometry.Record<?>> {

        @RequiredArgsConstructor
        @Getter
        @ToString
        public static class Record<T> {
            private final ElementGeometryType type;
            private final T geometryData;
        }
    }
}
