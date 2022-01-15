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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpenStreetMapStyleJsonPacker extends ScjdJsonPacker {

    public OpenStreetMapStyleJsonPacker(FeatureJSON featureJSON) {
        super(featureJSON);
    }

    @Override
    public void pack(ShapefileConversionResult conversion, Writer writer) throws IOException {

        boolean first = true;
        List<Geometry> subtractGeometries;
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
                    Geometry coastlineGeometry = (Geometry) mapBoundaryFeature.getDefaultGeometry();
                    if(coastlineGeometry instanceof LineString || coastlineGeometry instanceof MultiLineString) {
                        coastlineGeometry = FeatureGeometryUtils.lineStringToOuterEdgeOnlyPolygon(coastlineGeometry);
                    }

                    if(boundaryCollection != null) {
                        coastlineGeometry = FeatureGeometryUtils.getFeatureCollectionGeometryDifference(
                                coastlineGeometry, FeatureGeometryUtils.extractGeometries(boundaryCollection)
                        );
                    }

                    if(coastlineGeometry == null || coastlineGeometry.isEmpty()) continue;

                    if(first) first = false;
                    else writer.write(",");

                    SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(LayerDataType.해안선.getFeatureType());
                    featureBuilder.set(Constants.GEOMETRY_PROPERTY_NAME, coastlineGeometry);
                    SimpleFeature coastline = featureBuilder.buildFeature(conversion.getIndex() + "-coastline");
                    coastline = LayerDataType.해안선.toOsmStyleFeature(coastline, conversion.getIndex() + "-coastline");

                    writer.write(featureJSON.toString(coastline));
                    continue;

                case 시도_행정경계:
                    continue;

                case 도로경계:
                    SimpleFeatureCollection featureCollection = entry.getValue();
                    subtractGeometries = new ArrayList<>();

                    if (conversion.containsKey(LayerDataType.터널)) {
                        subtractGeometries.addAll(FeatureGeometryUtils.extractGeometries(conversion.get(LayerDataType.터널)));
                    }
                    if (conversion.containsKey(LayerDataType.입체교차부)) {
                        subtractGeometries.addAll(FeatureGeometryUtils.extractGeometries(conversion.get(LayerDataType.입체교차부)));
                    }
                    if (conversion.containsKey(LayerDataType.교량)) {
                        subtractGeometries.addAll(FeatureGeometryUtils.extractGeometries(
                                conversion.get(LayerDataType.교량),
                                f -> "road".equals(f.getAttribute("highway"))
                        ));
                    }

                    if(subtractGeometries.size() != 0) {
                        featureCollection = FeatureGeometryUtils.subtractPolygonsToPolygonCollection(
                                type.getOsmFeatureType(),
                                featureCollection, subtractGeometries,
                                i -> conversion.getIndex() + "-A0010000-" + i
                        );
                        entry.setValue(featureCollection);
                    }

                    if (conversion.containsKey(LayerDataType.안전지대)) {
                        entry.setValue(FeatureGeometryUtils.getFeatureCollectionGeometryDifference(
                                type.getOsmFeatureType(), entry.getValue(),
                                FeatureGeometryUtils.extractGeometries(
                                        conversion.get(LayerDataType.안전지대),
                                        f -> "yes".equals(f.getAttribute("crossing:island"))
                                )
                        ));
                    }
                    break;

                case 도로중심선:
                    subtractGeometries = new ArrayList<>();

                    if (conversion.containsKey(LayerDataType.터널)) {
                        subtractGeometries.addAll(FeatureGeometryUtils.extractGeometries(conversion.get(LayerDataType.터널)));
                    }
                    if (conversion.containsKey(LayerDataType.입체교차부)) {
                        subtractGeometries.addAll(FeatureGeometryUtils.extractGeometries(conversion.get(LayerDataType.입체교차부)));
                    }
                    if (conversion.containsKey(LayerDataType.교량)) {
                        subtractGeometries.addAll(FeatureGeometryUtils.extractGeometries(
                                conversion.get(LayerDataType.교량),
                                f -> "road".equals(f.getAttribute("highway"))
                        ));
                    }
                    if (conversion.containsKey(LayerDataType.안전지대)) {
                        subtractGeometries.addAll(FeatureGeometryUtils.extractGeometries(
                                conversion.get(LayerDataType.안전지대),
                                f -> "yes".equals(f.getAttribute("crossing:island"))
                        ));
                    }

                    if (subtractGeometries.size() != 0) {
                        entry.setValue(FeatureGeometryUtils.getFeatureCollectionGeometryDifference(
                                type.getOsmFeatureType(), entry.getValue(), subtractGeometries
                        ));
                    }
                    break;
            }

            SimpleFeatureIterator iterator = entry.getValue().features();
            while(iterator.hasNext()) {
                if(first) first = false;
                else writer.write(",");

                writer.write(featureJSON.toString(iterator.next()));
            }
        }

        writer.write("]}");
    }
}
