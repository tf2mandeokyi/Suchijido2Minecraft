package com.mndk.scjdmc.geojson.jsonpacker;

import com.mndk.scjdmc.geojson.converter.ShapefileConversionResult;
import com.mndk.scjdmc.scjd.LayerDataType;
import com.mndk.scjdmc.util.Constants;
import com.mndk.scjdmc.util.FeatureGeometryUtils;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.opengis.feature.simple.SimpleFeature;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class OpenStreetMapStyleJsonPacker extends ScjdJsonPacker {

    public OpenStreetMapStyleJsonPacker(FeatureJSON featureJSON) {
        super(featureJSON);
    }

    @Override
    public void pack(ShapefileConversionResult conversion, Writer writer) throws IOException {

        boolean first = true;
        writer.write("{\"type\":\"FeatureCollection\",\"features\":[");

        for(Map.Entry<LayerDataType, SimpleFeatureCollection> entry : conversion.entrySet()) {

            LayerDataType type = entry.getKey();
            if(!this.layerFilter.apply(type)) continue;

            switch(type) {

                // Build natural:coastline
                case 도곽선:
                    if(!this.indexCoastlineFilter.apply(conversion.getIndex())) continue;

                    SimpleFeatureCollection boundaryCollection = conversion.get(LayerDataType.시도_행정경계);
                    if(boundaryCollection == null) {
                        LOGGER.warn("No administrative boundary found! (" + conversion.getIndex() + ")");
                    }

                    SimpleFeature mapBoundaryFeature = entry.getValue().features().next();
                    Geometry geometry = (Geometry) mapBoundaryFeature.getDefaultGeometry();
                    if(geometry instanceof LineString || geometry instanceof MultiLineString) {
                        geometry = FeatureGeometryUtils.lineStringToOuterEdgeOnlyPolygon(geometry);
                    }

                    if(boundaryCollection != null) {
                        geometry = FeatureGeometryUtils.getFeatureCollectionGeometryDifference(
                                geometry, boundaryCollection
                        );
                    }

                    if(geometry == null) continue;

                    if(first) first = false;
                    else writer.write(",");

                    SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(LayerDataType.해안선.getFeatureType());
                    featureBuilder.set(Constants.GEOMETRY_PROPERTY_NAME, geometry);
                    SimpleFeature coastline = featureBuilder.buildFeature(conversion.getIndex() + "-coastline");
                    coastline = LayerDataType.해안선.toOsmStyleFeature(coastline, conversion.getIndex() + "-coastline");

                    writer.write(featureJSON.toString(coastline));
                    break;

                case 시도_행정경계:
                    break;

                case 도로경계:
                case 도로중심선:
                    if(conversion.containsKey(LayerDataType.터널)) {
                        entry.setValue(FeatureGeometryUtils.getFeatureCollectionGeometryDifference(
                                type.getOsmFeatureType(), entry.getValue(), conversion.get(LayerDataType.터널)
                        ));
                    }
                default:
                    SimpleFeatureIterator iterator = entry.getValue().features();
                    while(iterator.hasNext()) {
                        if(first) first = false;
                        else writer.write(",");

                        writer.write(featureJSON.toString(iterator.next()));
                    }
                    break;
            }
        }

        writer.write("]}");
    }
}
