package com.mndk.scjd2mc.core.scjd;

import com.mndk.scjd2mc.core.db.common.TableColumns;
import com.mndk.scjd2mc.core.scjd.elem.*;
import com.mndk.scjd2mc.core.scjd.geometry.GeometryShape;
import com.mndk.scjd2mc.core.scjd.geometry.LineString;
import com.mndk.scjd2mc.core.scjd.geometry.Point;
import com.mndk.scjd2mc.core.scjd.geometry.Polygon;
import com.mndk.scjd2mc.core.scjd.type.ElementDataType;
import com.mndk.scjd2mc.core.scjd.type.ElementGeometryType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

public class ScjdDataPayload {


    public static SuchijidoData combineVMapPayloads(
            Geometry geometryPayload,
            Data dataPayload,
            Map<String, String> options
    ) throws Exception {

        SuchijidoData result = new SuchijidoData();

        for(Map.Entry<Long, GeometryShape<?>> entry : geometryPayload.entrySet()) {
            long id = entry.getKey();
            final GeometryShape<?> shape = entry.getValue();
            Data.Record dataRecord = dataPayload.get(id);

            if(dataRecord == null) continue;

            Object[] dataRow = dataRecord.getDataRow();

            ElementGeometryType geometryType = shape.getType();
            ElementDataType dataType = dataRecord.getType();

            ScjdLayer layer = result.getLayer(dataType);
            TableColumns columns = dataType.getColumns();

            if(layer == null) {
                String[] stringColumns = new String[columns.getLength()];
                for(int i = 0; i < stringColumns.length; ++i) {
                    stringColumns[i] = columns.get(i).getName();
                }
                result.addLayer(layer = new ScjdLayer(dataType, stringColumns));
            }

            switch(geometryType) {
                case POINT:
                    if(dataType == ElementDataType.표고점) {
                        layer.add(new ScjdElevationPoint(layer, (Point) shape, dataRow));
                        continue;
                    }
                    break;
                case LINESTRING:
                    if(dataType == ElementDataType.등고선) {
                        layer.add(new ScjdContour(layer, (LineString) shape, dataRow));
                        continue;
                    }
                    break;
                case POLYGON:
                    if(dataType == ElementDataType.건물) {
                        layer.add(new ScjdBuilding(layer, Long.toString(id), (Polygon) shape, dataRow,
                                options.containsKey("gen-building-shells")));
                        continue;
                    }
                    break;
            }

            ScjdElement<?> element = new ScjdElement<>(layer, Long.toString(id), shape, dataRow);
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


    public static class Geometry extends HashMap<Long, GeometryShape<?>> { }
}
