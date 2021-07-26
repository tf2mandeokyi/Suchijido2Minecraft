package com.mndk.scjd2mc.core.db;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mndk.scjd2mc.core.scjd.SuchijidoData;
import com.mndk.scjd2mc.core.scjd.elem.ScjdElement;
import com.mndk.scjd2mc.core.scjd.elem.ScjdLayer;
import com.mndk.scjd2mc.core.util.shape.BoundingBoxDouble;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.ValidationOptions;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MongoDBManager {


    private static final String MAIN_DATABASE_NAME = "suchijido";
    private static final String MAIN_COLLECTION_NAME = "elements";



    private static final CreateCollectionOptions CREATE_COLLECTION_OPTIONS;
    private static final ValidationOptions VALIDATION_OPTIONS;

    private static final Gson GSON = new GsonBuilder().create();



    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Document> collection;



    public static void connect(String url) {
        mongoClient = new MongoClient(new MongoClientURI(url));
        database = mongoClient.getDatabase(MAIN_DATABASE_NAME);
        if(!collectionExists()) {
            database.createCollection(MAIN_COLLECTION_NAME, CREATE_COLLECTION_OPTIONS);
        }
        collection = database.getCollection(MAIN_COLLECTION_NAME);
    }



    private static boolean collectionExists() {
        MongoIterable<String> collectionNames = database.listCollectionNames();
        for (final String name : collectionNames) {
            if (name.equalsIgnoreCase(MAIN_COLLECTION_NAME)) {
                return true;
            }
        }
        return false;
    }



    public static void refreshDataByMapIndex(SuchijidoData data, String map_index) {
        List<Document> inserts = new ArrayList<>();
        for(ScjdLayer layer : data.getLayers()) {
            for(ScjdElement element : layer) {
                Document doc = element.toBsonDocument(map_index);
                if(doc != null) {
                    inserts.add(element.toBsonDocument(map_index));
                }
            }
        }
        collection.deleteMany(new Document().append("map_index", map_index));
        collection.insertMany(inserts);
    }



    public static MongoIterable<Document> getDataByIndex(String map_index) {
        return collection.find(new Document().append("map_index", map_index));
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



    public static List<JsonObject> convertMongoDocumentsToGeoJsonString(MongoIterable<Document> iterable) {

        List<JsonObject> result = new ArrayList<>();

        for(Document document : iterable) {

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

            result.add(j);
        }

        return result;
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
