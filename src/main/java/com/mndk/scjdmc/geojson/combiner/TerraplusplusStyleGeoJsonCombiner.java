package com.mndk.scjdmc.geojson.combiner;

import com.google.gson.*;
import com.mndk.scjdmc.scjd.LayerDataType;
import com.mndk.scjdmc.scjd.MapIndexManager;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.geometry.BoundingBox;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class TerraplusplusStyleGeoJsonCombiner extends GeoJsonCombiner {

    private static final double SCALE = 64;

    private static final Pattern FOLDER_PATTERN = Pattern.compile("^([A-Z]\\d{7})(_[A-Za-z_]*)?$");

    private final Gson gson;

    public TerraplusplusStyleGeoJsonCombiner() {
        super();
        this.gson = new GsonBuilder().create();
    }

    @Override
    public void combine(File source, File destination) {
        File[] indexJsonFiles = source.listFiles(file -> {
            String fileName = file.getName();
            if(!file.isFile() || !fileName.endsWith(".json")) return false;
            return MapIndexManager.validateIndexName(fileName.substring(0, fileName.lastIndexOf(".")));
        });
        assert indexJsonFiles != null;

        Set<String> availableIndexes = new HashSet<>();

        // Read all the layer folders
        for(File file : indexJsonFiles) {
            String fileName = file.getName();
            availableIndexes.add(fileName.substring(0, fileName.lastIndexOf(".")));
        }

        // Calculate total bounding box
        double minLat = Double.POSITIVE_INFINITY, minLon = Double.POSITIVE_INFINITY,
                maxLat = Double.NEGATIVE_INFINITY, maxLon = Double.NEGATIVE_INFINITY;
        for(String index : availableIndexes) {
            int[] tilePos = MapIndexManager.indexToPosition(index);
            int scale = MapIndexManager.getTileScale(index);

            double[] leftBottom = MapIndexManager.positionToLongLat(tilePos, scale, false);
            double[] upRight = MapIndexManager.positionToLongLat(new int[] { tilePos[0] + 1, tilePos[1] + 1 }, scale, false);
            if(minLon > leftBottom[0]) minLon = leftBottom[0];
            if(minLat > leftBottom[1]) minLat = leftBottom[1];
            if(maxLon < upRight[0]) maxLon = upRight[0];
            if(maxLat < upRight[1]) maxLat = upRight[1];
        }

        int minX = (int) Math.floor(minLon * SCALE), maxX = (int) Math.ceil(maxLon * SCALE);
        int minY = (int) Math.floor(minLat * SCALE), maxY = (int) Math.ceil(maxLat * SCALE);

        BoundingBox bbox;
        for(int y = maxY; y >= minY; y--) for(int x = minX; x <= maxX; x++) {
            JsonArray featureList = new JsonArray();

            bbox = new ReferencedEnvelope(x / SCALE, (x+1) / SCALE, y / SCALE, (y+1) / SCALE, null);

            List<String> indexes = MapIndexManager.getContainingIndexes(bbox, 5000); // Scale specification; Fix this issue
            for(String index : indexes) {
                if(!availableIndexes.contains(index)) continue;

                File indexFile = new File(source, index + ".json");
                if(!indexFile.isFile()) continue;

                try(FileReader reader = new FileReader(indexFile)) {
                    JsonObject readFeatureCollection = JsonParser.parseReader(reader).getAsJsonObject();
                    JsonArray featureArray = readFeatureCollection.getAsJsonArray("features");
                    for(JsonElement featureElement : featureArray) {
                        try {
                            JsonObject feature = featureElement.getAsJsonObject();

                            String id = feature.get("id").getAsString();
                            int firstDashIndex = id.indexOf("-"), lastDashIndex = id.lastIndexOf("-");
                            if(firstDashIndex != lastDashIndex) {
                                // feature is not a coastline
                                String layerName = id.substring(id.indexOf("-") + 1, lastDashIndex);
                                LayerDataType type = LayerDataType.fromLayerName(layerName);
                                if (!this.layerFilter.apply(type)) continue;
                            }

                            JsonObject geometry = feature.getAsJsonObject("geometry");
                            geometry = validateGeometryObject(geometry);
                            feature.add("geometry", geometry);
                            BoundingBox featureBoundingBox = getBoundingBoxFromGeometryObject(geometry);
                            if (bbox.intersects(featureBoundingBox)) {
                                featureList.add(feature);
                            }
                        } catch(ClassCastException ignored) {}
                    }
                } catch (Exception e) {
                    LOGGER.error("Error reading " + indexFile, e);
                }
            }
            if(featureList.size() == 0) continue;

            JsonObject featureCollection = new JsonObject();
            featureCollection.addProperty("type", "FeatureCollection");
            featureCollection.add("features", featureList);

            File file = new File(destination, "tile/" + x + "/" + y + ".json");
            if(!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                new IOException("Failed to create folder").printStackTrace();
                continue;
            }
            try(FileWriter writer = new FileWriter(file)) {
                this.gson.toJson(featureCollection, writer);
                writer.flush();
                this.onConversionCompleteFunction.accept(x + "," + y, bbox);
            } catch (Exception e) {
                LOGGER.error("Error writing " + file, e);
            }
        }
    }


    private static JsonObject validateGeometryObject(JsonObject geometryObject) {
        String geometryType = geometryObject.get("type").getAsString();
        JsonArray newGeometryArray = validateGeometry(
                geometryObject.getAsJsonArray("coordinates"),
                getArrayDepthFromGeometryType(geometryType)
        );
        JsonObject newGeometryObject = new JsonObject();
        newGeometryObject.addProperty("type", geometryType);
        newGeometryObject.add("coordinates", newGeometryArray);
        return newGeometryObject;
    }

    private static JsonArray validateGeometry(JsonArray geometry, int depth) {
        if(depth == 0) return geometry;

        JsonArray newGeometry = new JsonArray();
        if(depth == 1) {
            if (geometry.size() == 0) return geometry;

            JsonArray point = geometry.get(0).getAsJsonArray();
            newGeometry.add(point);

            double prevLat = point.get(0).getAsDouble(), prevLon = point.get(1).getAsDouble(), lat, lon;
            for (int i = 1; i < geometry.size(); i++) {
                // Duplicate point detection
                point = geometry.get(i).getAsJsonArray();
                lat = point.get(0).getAsDouble();
                lon = point.get(1).getAsDouble();
                if (prevLat == lat && prevLon == lon) continue;
                newGeometry.add(point);
                prevLat = lat;
                prevLon = lon;
            }
        }
        else {
            for (JsonElement element : geometry) {
                JsonArray validation = validateGeometry(element.getAsJsonArray(), depth - 1);
                newGeometry.add(validation);
            }
        }

        return newGeometry;
    }


    private static BoundingBox getBoundingBoxFromGeometryObject(JsonObject geometryObject) {
        return getBoundingBoxFromGeometry(
                geometryObject.getAsJsonArray("coordinates"),
                getArrayDepthFromGeometryType(geometryObject.get("type").getAsString())
        );
    }

    private static BoundingBox getBoundingBoxFromGeometry(JsonArray geometryArray, int depth) {
        if(depth == 0) {
            double lon = geometryArray.get(0).getAsDouble(), lat = geometryArray.get(1).getAsDouble();
            return new ReferencedEnvelope(lon, lon, lat, lat, null);
        }
        else {
            BoundingBox result = null;
            for(JsonElement element : geometryArray) {
                JsonArray array = element.getAsJsonArray();
                BoundingBox bbox = getBoundingBoxFromGeometry(array, depth - 1);
                if(result == null) result = bbox;
                else result.include(bbox);
            }
            return result;
        }
    }


    private static int getArrayDepthFromGeometryType(String geometryType) {
        switch(geometryType) {
            case "Point": return 0;
            case "LineString": case "MultiPoint": return 1;
            case "Polygon": case "MultiLineString": return 2;
            case "MultiPolygon": return 3;
            default: throw new IllegalArgumentException("Illegal type: " + geometryType);
        }
    }

}
