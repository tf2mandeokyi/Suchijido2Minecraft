package com.mndk.scjdmc.geojson.jsonpacker;

import com.mndk.scjdmc.geojson.converter.ShapefileConversionResult;
import com.mndk.scjdmc.scjd.LayerDataType;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeature;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Map;

public class OpenStreetMapStyleJsonPacker extends JsonPacker {

    private final GeometryFactory geometryFactory;

    public OpenStreetMapStyleJsonPacker(FeatureJSON featureJSON) {
        super(featureJSON);
        this.geometryFactory = new GeometryFactory();
    }

    @Override
    public void pack(ShapefileConversionResult result, Writer writer) throws IOException {

        boolean first = true;
        writer.write("{\"type\":\"FeatureCollection\",\"features\":[");

        for(Map.Entry<LayerDataType, SimpleFeatureCollection> entry : result.entrySet()) {

            LayerDataType type = entry.getKey();

            if(type == LayerDataType.도곽선) {

                SimpleFeatureCollection boundaryCollection = result.get(LayerDataType.시도_행정경계);
                if(boundaryCollection == null) {
                    LOGGER.warn("No administrative boundary found! (" + result.getIndex() + ")");
                    continue;
                }

                if(first) first = false;
                else writer.write(",");

                // Build natural:coastline
                SimpleFeature mapBoundaryFeature = entry.getValue().features().next();
                Geometry geometry = (Geometry) mapBoundaryFeature.getDefaultGeometry();
                if(geometry instanceof LineString || geometry instanceof MultiLineString) {
                    Coordinate[] coordinates = geometry.getCoordinates();
                    if(!coordinates[0].equals2D(coordinates[coordinates.length - 1])) {
                        coordinates = Arrays.copyOf(coordinates, coordinates.length + 1);
                        coordinates[coordinates.length - 1] = coordinates[0];
                    }
                    LinearRing linearRing = new LinearRing(
                            geometryFactory.getCoordinateSequenceFactory().create(coordinates),
                            geometryFactory
                    );
                    geometry = new Polygon(linearRing, new LinearRing[0], geometryFactory);
                }

                SimpleFeatureIterator boundaryIterator = boundaryCollection.features();
                while(boundaryIterator.hasNext()) {
                    Geometry boundaryGeometry = (Geometry) boundaryIterator.next().getDefaultGeometry();
                    geometry = geometry.difference(boundaryGeometry.buffer(0.000005));
                }

                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(LayerDataType.해안선.getFeatureType());
                featureBuilder.set("geometry", geometry);
                SimpleFeature coastline = featureBuilder.buildFeature(result.getIndex() + "-coastline");
                coastline = LayerDataType.해안선.toOsmStyleFeature(coastline, result.getIndex() + "-coastline");

                writer.write(featureJSON.toString(coastline));
            }
            else if (type != LayerDataType.시도_행정경계) {
                SimpleFeatureIterator iterator = entry.getValue().features();
                while(iterator.hasNext()) {
                    if(first) first = false;
                    else writer.write(",");

                    writer.write(featureJSON.toString(iterator.next()));
                }
            }
        }

        writer.write("]}");
    }
}
