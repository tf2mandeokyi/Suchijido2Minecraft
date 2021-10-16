package com.mndk.scjd2mc.core.db;

import com.google.gson.*;
import com.mndk.scjd2mc.core.scjd.ScjdIndexManager;
import com.mndk.scjd2mc.core.scjd.SuchijidoData;
import com.mndk.scjd2mc.core.scjd.elem.ScjdElement;
import com.mndk.scjd2mc.core.scjd.elem.ScjdLayer;
import com.mndk.scjd2mc.core.scjd.reader.SuchijidoFileReader;
import com.mndk.scjd2mc.core.util.ProgressGui;
import com.mndk.scjd2mc.core.util.shape.BoundingBoxDouble;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.ValidationOptions;
import org.bson.Document;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MongoDBManager {


    private static final String DEFAULT_DATABASE_NAME = "suchijido";
    private static final String DEFAULT_COLLECTION_NAME = "elements";



    private static final CreateCollectionOptions CREATE_COLLECTION_OPTIONS;
    private static final ValidationOptions VALIDATION_OPTIONS;

    private static final Gson GSON = new GsonBuilder().create();



    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Document> collection;



    public static void connect(String url, String databaseName, String collectionName) {
        mongoClient = new MongoClient(new MongoClientURI(url));
        database = mongoClient.getDatabase(databaseName);
        if(!collectionExists(collectionName)) {
            database.createCollection(collectionName, CREATE_COLLECTION_OPTIONS);
        }
        collection = database.getCollection(collectionName);
    }

    public static void connect(String url) {
        connect(url, DEFAULT_DATABASE_NAME, DEFAULT_COLLECTION_NAME);
    }



    public static boolean isConnected() {
        return collection != null;
    }



    private static boolean collectionExists(String collectionName) {
        MongoIterable<String> collectionNames = database.listCollectionNames();
        for (final String name : collectionNames) {
            if (name.equalsIgnoreCase(collectionName)) {
                return true;
            }
        }
        return false;
    }



    public static void refreshDataByMapIndex(SuchijidoData data, String mapIndex) {
        List<Document> inserts = new ArrayList<>();
        for(ScjdLayer layer : data.getLayers()) {
            for(ScjdElement<?> element : layer) {
                Document doc = element.toBsonDocument(mapIndex);
                if(doc != null) {
                    inserts.add(element.toBsonDocument(mapIndex));
                }
            }
        }
        collection.deleteMany(new Document().append("map_index", mapIndex));
        if(inserts.size() != 0) {
            collection.insertMany(inserts);
        }
    }



    public static MongoIterable<Document> getDataByIndex(String mapIndex) {
        return collection.find(new Document().append("map_index", mapIndex));
    }



    public static MongoIterable<Document> getDataByBoundingBox(BoundingBoxDouble bbox) {
        return collection.find(new Document()
                .append("$and", Arrays.asList(
                        new Document().append("bounds.zmax", new Document().append("$gte", bbox.zmin)),
                        new Document().append("bounds.zmin", new Document().append("$lte", bbox.zmax)),
                        new Document().append("bounds.xmax", new Document().append("$gte", bbox.xmin)),
                        new Document().append("bounds.xmin", new Document().append("$lte", bbox.xmax))
                ))
        );
    }



    public static MongoIterable<Document> getDataByXYTileCoordinate(int x, int y, double zFactor) {
        return getDataByBoundingBox(
                new BoundingBoxDouble(x / zFactor, y / zFactor, (x + 1) / zFactor, (y + 1) / zFactor)
        );
    }



    public static void insertMultipleFiles(@Nonnull Collection<File> files, SuchijidoFileReader reader) throws Exception {

        ExecutorService es = Executors.newCachedThreadPool();

        List<Callable<Object>> tasks = new ArrayList<>();

        for(File file : files) {
            tasks.add(Executors.callable(() -> {
                String mapIndex = ScjdIndexManager.getMapIndexFromFileName(file.getName());

                System.out.println(file.getName() + " -> " + mapIndex);

                long read_t1 = System.currentTimeMillis();
                final SuchijidoData fileResult;
                try {
                    fileResult = reader.parse(file);
                } catch (Exception ignored) {
                    return;
                }
                long read_t2 = System.currentTimeMillis();
                final double read = (read_t2 - read_t1) / 1000.;

                long write_t1 = System.currentTimeMillis();
                MongoDBManager.refreshDataByMapIndex(fileResult, mapIndex);
                long write_t2 = System.currentTimeMillis();
                double write = (write_t2 - write_t1) / 1000.;

                double total = ((read_t2 - read_t1) + (write_t2 - write_t1)) / 1000.;

                System.out.println(file.getName() + "\n ㄴ Read: " + read + "s | Write: " + write + "s | Total: " + total + "s");
            }));
        }

        es.invokeAll(tasks);
    }



    public static BoundingBoxDouble getTotalDataBoundingBox() {
        MongoIterable<Document> all = collection.find();
        BoundingBoxDouble bbox = BoundingBoxDouble.ILLEGAL_INFINITE;
        for(Document document : all) {
            Document boundsDocument = document.get("bounds", Document.class);
            BoundingBoxDouble elementBBox = new BoundingBoxDouble(
                    boundsDocument.getDouble("xmin"),
                    boundsDocument.getDouble("zmin"),
                    boundsDocument.getDouble("xmax"),
                    boundsDocument.getDouble("zmax")
            );
            bbox = bbox.or(elementBBox);
        }
        return bbox;
    }



    public static void putAllDataToTiledGeoJsonFiles(
            File rootPath, boolean separateLongElements, boolean asFeatureCollections, boolean debugGuiScreen
    ) throws Exception {
        putAllDataToTiledGeoJsonFiles(rootPath, 64, separateLongElements, asFeatureCollections, debugGuiScreen);
    }

    public static void putAllDataToTiledGeoJsonFiles(
            File rootPath, int zFactor, boolean separateLongElements, boolean asFeatureCollections, boolean debugGuiScreen
    ) throws Exception {

        System.out.println("Fetching bounding box...");

        BoundingBoxDouble bbox = getTotalDataBoundingBox();

        System.out.println("Done (x: [" + bbox.xmin + ", " + bbox.xmax + "], z: [" + bbox.zmin + ", " + bbox.zmax + "])");

        int xMin = (int) Math.floor(bbox.xmin * zFactor),
            yMin = (int) Math.floor(bbox.zmin * zFactor),
            xMax = (int) Math.ceil(bbox.xmax * zFactor),
            yMax = (int) Math.ceil(bbox.zmax * zFactor);

        System.out.println(" ㄴ xTile: [" + xMin + ", " + xMax + "], yTile: [" + yMin + ", " + yMax + "])");

        final ProgressGui visualizer;
        if(debugGuiScreen) {
            visualizer = new ProgressGui(xMin, yMin, xMax, yMax, 10);
        } else {
            visualizer = null;
        }

        ExecutorService es = Executors.newFixedThreadPool(12);
        List<Callable<Object>> tasks = new ArrayList<>();

        for(int x = xMin; x <= xMax; ++x) {
            for (int y = yMin; y <= yMax; ++y) {

                int finalX = x;
                int finalY = y;
                tasks.add(Executors.callable(() -> {
                    File file = new File(rootPath, "tile/" + finalX + "/" + finalY + ".json");

                    if (!file.getParentFile().isDirectory() && !file.getParentFile().mkdirs()) {
                        System.out.println("Folder creation failed");
                    }

                    if (file.exists() && !file.delete()) {
                        System.out.println("Failed to delete the old file");
                    }

                    MongoIterable<Document> iterable = MongoDBManager.getDataByXYTileCoordinate(finalX, finalY, zFactor);
                    List<Document> documents = new ArrayList<>();
                    iterable.forEach((Consumer<? super Document>) documents::add);

                    if (documents.size() == 0) {
                        if(visualizer != null) visualizer.setStatus(finalX, finalY, ProgressGui.NO_DATA);
                        else System.out.println("No data found for " + file);
                        return;
                    }

                    try (FileWriter writer = new FileWriter(file)) {

                        if (asFeatureCollections) {
                            JsonObject featureCollection = new JsonObject();
                            featureCollection.addProperty("type", "FeatureCollection");
                            JsonArray features = new JsonArray();
                            for (Document document : iterable) {
                                JsonObject feature = MongoDBManager.mongoDocumentToGeoJson(document);
                                features.add(feature);
                            }
                            featureCollection.add("features", features);
                            writer.write(featureCollection.toString());
                        } else {
                            for (Document document : iterable) {
                                JsonObject feature = MongoDBManager.mongoDocumentToGeoJson(document);
                                String featureString = feature.toString();
                                if (separateLongElements && featureString.length() > 1700) {
                                    String _id = JsonParser.parseString(featureString).getAsJsonObject().get("id").getAsString();
                                    String referenceDir = "way/" +
                                            _id.substring(0, 8) + "/" +
                                            _id.substring(8, 18) + "/" +
                                            _id.substring(18) + ".json";
                                    File reference = new File(rootPath, referenceDir);
                                    File referenceParent = reference.getParentFile();
                                    if (!referenceParent.isDirectory() && !referenceParent.mkdirs()) {
                                        System.out.println("Failed to create the directory");
                                    }
                                    try (FileWriter referenceWriter = new FileWriter(reference)) {
                                        referenceWriter.write(featureString);
                                    }
                                    writer.write("{\"type\":\"Reference\",\"location\":\"" + referenceDir + "\"}\n");
                                } else {
                                    writer.write(feature + "\n");
                                }
                            }
                        }
                        writer.flush();
                        if(visualizer != null) visualizer.setStatus(finalX, finalY, ProgressGui.SUCCESS);
                        else System.out.println("Saved " + file);
                    } catch (IOException ignored) {
                        if(visualizer != null) visualizer.setStatus(finalX, finalY, ProgressGui.ERROR);
                    }
                }));
            }
        }

        es.invokeAll(tasks);

        System.out.println("Done.");
    }


    public static List<JsonObject> convertMongoDocumentsToGeoJsonList(MongoIterable<Document> iterable) {

        List<JsonObject> result = new ArrayList<>();

        for(Document document : iterable) {
            result.add(mongoDocumentToGeoJson(document));
        }

        return result;
    }



    public static JsonObject mongoDocumentToGeoJson(Document document) {

        // Could've just directly converted the document into JsonObject
        // if it wasn't T++'s "GeoJson's first key should be 'type'" validation...

        JsonObject j = new JsonObject();

        j.addProperty("type", document.getString("type"));

        JsonObject geometryJson = new JsonObject();
        Document geometryDoc = document.get("geometry", Document.class);
        geometryJson.addProperty("type", geometryDoc.getString("type"));
        geometryJson.add("coordinates", GSON.toJsonTree(geometryDoc.get("coordinates")));
        j.add("geometry", geometryJson);

        j.add("properties", GSON.toJsonTree(document.get("properties")));

        j.addProperty("id", document.getObjectId("_id").toString());

        return j;
    }



    public static void close() {
        mongoClient.close();
    }



    static {
        VALIDATION_OPTIONS = new ValidationOptions().validator(new Document()
                .append("$jsonSchema", new Document()
                        .append("bsonType", "object")
                        .append("required", Arrays.asList("type", "geometry", "bounds", "properties", "map_index"))
                        .append("properties", new Document()
                                .append("type", new Document().append("bsonType", "string"))
                                .append("geometry", new Document()
                                        .append("bsonType", "object")
                                        .append("required", Arrays.asList("type", "coordinates"))
                                        .append("properties", new Document()
                                                .append("type", new Document().append("bsonType", "string"))
                                                .append("coordinates", new Document().append("bsonType", Arrays.asList("double", "array")))
                                        )
                                )
                                .append("bounds", new Document()
                                        .append("bsonType", "object")
                                        .append("required", Arrays.asList("xmin", "xmax", "zmin", "zmax"))
                                        .append("properties", new Document()
                                                .append("xmin", new Document().append("bsonType", "double"))
                                                .append("xmax", new Document().append("bsonType", "double"))
                                                .append("zmin", new Document().append("bsonType", "double"))
                                                .append("zmax", new Document().append("bsonType", "double"))
                                        )
                                )
                                .append("properties", new Document().append("bsonType", "object"))
                                .append("map_index", new Document().append("bsonType", "string"))
                        )
                )
        );
        CREATE_COLLECTION_OPTIONS = new CreateCollectionOptions().validationOptions(VALIDATION_OPTIONS);
    }

}
