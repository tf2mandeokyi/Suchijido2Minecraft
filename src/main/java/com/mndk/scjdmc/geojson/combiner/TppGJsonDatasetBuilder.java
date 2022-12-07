package com.mndk.scjdmc.geojson.combiner;

import com.google.gson.*;
import com.mndk.scjdmc.scjd.LayerDataType;
import com.mndk.scjdmc.util.ScjdMapIndexUtils;
import com.mndk.scjdmc.util.GeometryJsonUtils;
import com.mndk.scjdmc.util.file.DirectoryManager;
import lombok.Setter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.geometry.BoundingBox;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TppGJsonDatasetBuilder extends GJsonDatasetBuilder {

    private static final double SCALE = 64;

    private final Gson gson;
    @Setter private boolean separateCoastlines = false;

    public TppGJsonDatasetBuilder() {
        super();
        this.gson = new GsonBuilder().create();
    }

    @Override
    public void combine(File source, File destination) throws IOException {

        File tileFolder = new File(destination, "tile");
        File coastlineFolder = new File(destination, "coastline");

        DirectoryManager.createFolder(tileFolder, "tile");
        if(separateCoastlines) DirectoryManager.createFolder(tileFolder, "coastline");

        File[] indexJsonFiles = source.listFiles(file -> {
            String fileName = file.getName();
            if(!file.isFile() || !fileName.endsWith(".json")) return false;
            return ScjdMapIndexUtils.validateIndexName(fileName.substring(0, fileName.lastIndexOf(".")));
        });
        assert indexJsonFiles != null;
        if(indexJsonFiles.length == 0) return;

        // Get all available indexes
        Set<String> availableIndexes = new HashSet<>(), writtenCoastlines = new HashSet<>();
        for(File file : indexJsonFiles) {
            String fileName = file.getName();
            availableIndexes.add(fileName.substring(0, fileName.lastIndexOf(".")));
        }

        // Calculate total bounding box
        BoundingBox bbox = null;
        for(String index : availableIndexes) {
            if(bbox == null) bbox = ScjdMapIndexUtils.getBoudingBox(index, false);
            else bbox.include(ScjdMapIndexUtils.getBoudingBox(index, false));
        }

        int minX = (int) Math.floor(bbox.getMinX() * SCALE), maxX = (int) Math.ceil(bbox.getMaxX() * SCALE);
        int minY = (int) Math.floor(bbox.getMinY() * SCALE), maxY = (int) Math.ceil(bbox.getMaxY() * SCALE);

        for(int y = maxY; y >= minY; y--) for(int x = minX; x <= maxX; x++) {
            JsonArray featureList = new JsonArray();

            bbox = new ReferencedEnvelope(x / SCALE, (x + 1) / SCALE, y / SCALE, (y + 1) / SCALE, null);

            List<String> indexes = ScjdMapIndexUtils.getContainingIndexes(bbox, 5000); // Scale specification; Fix this issue
            for (String index : indexes) {
                if (!availableIndexes.contains(index)) continue;

                File indexFile = new File(source, index + ".json");
                if (!indexFile.isFile()) continue;

                try (FileReader reader = new FileReader(indexFile)) {
                    JsonObject readFeatureCollection = JsonParser.parseReader(reader).getAsJsonObject();
                    JsonArray featureArray = readFeatureCollection.getAsJsonArray("features");
                    for (JsonElement featureElement : featureArray) {
                        JsonObject feature = featureElement.getAsJsonObject();
                        String id = feature.get("id").getAsString();

                        int firstDashIndex = id.indexOf("-"), lastDashIndex = id.lastIndexOf("-");
                        if (firstDashIndex != lastDashIndex) {
                            // feature is not a coastline
                            String layerName = id.substring(firstDashIndex + 1, lastDashIndex);
                            LayerDataType type = LayerDataType.fromLayerName(layerName);
                            if (!this.layerFilter.apply(type)) continue;
                        } else if (id.contains("coastline") && this.separateCoastlines) {
                            // feature is a coastline
                            if (!writtenCoastlines.contains(index)) {
                                // coastline has not been written yet
                                File coastlineFile = new File(coastlineFolder, index + ".json");
                                try (FileWriter writer = new FileWriter(coastlineFile)) {
                                    this.gson.toJson(feature, writer);
                                    writer.flush();
                                    writtenCoastlines.add(index);
                                } catch (Exception e) {
                                    LOGGER.error("Error writing " + coastlineFile, e);
                                }
                            }

                            JsonObject reference = new JsonObject();
                            reference.addProperty("type", "Reference");
                            reference.addProperty("location", "coastline/" + index + ".json");
                            featureList.add(reference);
                            continue;
                        }

                        JsonElement geometryElement = feature.get("geometry");
                        if (geometryElement == null || geometryElement.isJsonNull()) continue;

                        JsonObject geometry = geometryElement.getAsJsonObject();
                        BoundingBox featureBoundingBox = GeometryJsonUtils.getJsonGeometryBoundingBox(geometry);
                        if (bbox.intersects(featureBoundingBox)) {
                            // The geometry might have overlapping points, so it's fixing it here.
                            geometry = GeometryJsonUtils.validateJsonGeometry(geometry);
                            if(geometry == null) continue;
                            feature.add("geometry", geometry);
                            featureList.add(feature);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Error reading " + indexFile, e);
                }
            }
            if (featureList.size() == 0) continue;

            JsonObject featureCollection = new JsonObject();
            featureCollection.addProperty("type", "FeatureCollection");
            featureCollection.add("features", featureList);

            File file = new File(tileFolder, x + "/" + y + ".json");
            DirectoryManager.createParentFolders(file);

            try (FileWriter writer = new FileWriter(file)) {
                this.gson.toJson(featureCollection, writer);
                writer.flush();
                this.onConversionCompleteFunction.accept(file, bbox);
            } catch (Exception e) {
                LOGGER.error("Error writing " + file, e);
            }
        }
    }
}
