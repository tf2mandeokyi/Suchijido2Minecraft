package com.mndk.scjdmc.typeconverter;

import com.mndk.scjdmc.Constants;
import com.mndk.scjdmc.scjd.LayerDataType;
import com.mndk.scjdmc.util.ExceptionThrowableRunner;
import com.mndk.scjdmc.util.ScjdMapIndexUtils;
import com.mndk.scjdmc.util.function.LayerFilterFunction;
import lombok.AllArgsConstructor;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.files.FileReader;
import org.geotools.data.shapefile.files.ShpFileType;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.store.ReprojectingFeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.File;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;


@AllArgsConstructor
public class ShpScjd2OsmFeature implements FileReader {

    protected final LayerFilterFunction layerFilter;

    public SimpleFeatureCollection convert(String mapIndex, File shpFile, String charset) throws IOException {

        LayerDataType layerDataType = LayerDataType.fromLayerName(shpFile.getName());
        if(!this.layerFilter.apply(layerDataType)) return null;

        AtomicReference<DataStore> dataStore = new AtomicReference<>();
        AtomicReference<SimpleFeatureCollection> featureCollection = new AtomicReference<>();

        handleDbfCorruption(shpFile, charset, () -> {
            Map<String, String> params = new HashMap<>();
            params.put("url", shpFile.toURI().toString());
            params.put("charset", charset);
            dataStore.set(DataStoreFinder.getDataStore(params));

            String[] typeNames = dataStore.get().getTypeNames();
            SimpleFeatureSource featureSource = dataStore.get().getFeatureSource(typeNames[0]);
            SimpleFeatureCollection result = featureSource.getFeatures();

            if(!result.isEmpty()) {
                featureCollection.set(applyProjection(mapIndex, featureSource.getFeatures()));
            }
        });

        String layerName = LayerDataType.findLayerTypeString(shpFile.getName());
        return toOsmStyleFeatureCollection(
                shpFile, featureCollection.get(), dataStore.get(),
                i -> mapIndex + "-" + layerName + "-" + i
        );
    }


    private boolean checkDbfReadable(File shpFile, String charset) throws IOException {
        ShpFiles shpFiles = new ShpFiles(shpFile);
        try(ReadableByteChannel dbfChannel = shpFiles.getReadChannel(ShpFileType.DBF, this)) {
            DbaseFileHeader dbfHeader = new DbaseFileHeader(Charset.forName(charset));
            dbfHeader.readHeader(dbfChannel);
        } catch(IllegalArgumentException e) {
            return false;
        }
        return true;
    }


    private void handleDbfCorruption(
            File shpFile, String charset, ExceptionThrowableRunner<IOException> callback
    ) throws IOException {
        boolean isDbfFileReadable = this.checkDbfReadable(shpFile, charset);
        Path dbfFilePath = null, newDbfFilePath = null;

        if(!isDbfFileReadable) {
            // DBF file is corrupted; renaming temporarily so that FeatureSource#getFeatures() won't read it
            String sourcePath = shpFile.getPath();
            dbfFilePath = Paths.get(sourcePath.substring(0, sourcePath.lastIndexOf(".")) + ".dbf");
            newDbfFilePath = Paths.get(sourcePath.substring(0, sourcePath.lastIndexOf(".")) + ".dbf~");
            Files.move(dbfFilePath, newDbfFilePath);
        }

        callback.run();

        if(!isDbfFileReadable) {
            Files.move(newDbfFilePath, dbfFilePath);
        }
    }


    private static SimpleFeatureCollection applyProjection(String mapIndex, SimpleFeatureCollection featureCollection) {

        SimpleFeatureCollection result;

        // CRS
        CoordinateReferenceSystem indexCrs = ScjdMapIndexUtils.getCoordinateReferenceSystemFromIndex(mapIndex);
        CoordinateReferenceSystem sourceCrs = featureCollection.getSchema().getGeometryDescriptor()
                .getCoordinateReferenceSystem();

        // Expected bounding box
        BoundingBox bbox = ScjdMapIndexUtils.getBoudingBox(mapIndex, true);

        // Try #1: Apply with .prj file
        if(sourceCrs != null) {
            result = new ReprojectingFeatureCollection(featureCollection, sourceCrs, Constants.CRS84);
            if(result.getBounds().intersects(bbox)) return result;

            // Try #1-1: Apply with .prj file with xy coordinates swapped
            result = new ReprojectingFeatureCollection(
                    new ReprojectingFeatureCollection(featureCollection, Constants.EPSG4326, Constants.CRS84),
                    sourceCrs, Constants.CRS84
            );
            if(result.getBounds().intersects(bbox)) return result;
        }

        // Try #2: Use CRS from index name
        if(indexCrs == null) throw new RuntimeException("Illegal index");
        result = new ReprojectingFeatureCollection(featureCollection, indexCrs, Constants.CRS84);
        if(result.getBounds().intersects(bbox)) return result;

        // Try #2-1: Use CRS from index name with xy coordinates swapped
        result = new ReprojectingFeatureCollection(
                new ReprojectingFeatureCollection(featureCollection, Constants.EPSG4326, Constants.CRS84),
                indexCrs, Constants.CRS84
        );
        if(result.getBounds().intersects(bbox)) return result;

        throw new RuntimeException("Failed to analyze projection; [" + mapIndex + "] : " + result.getBounds() + ", " + bbox);
    }


    private SimpleFeatureCollection toOsmStyleFeatureCollection(
            File shpFile, SimpleFeatureCollection featureCollection, DataStore dataStore,
            Function<Integer, String> idGenerator
    ) {

        LayerDataType layerDataType = LayerDataType.fromLayerName(shpFile.getName());
        SimpleFeatureType featureType = layerDataType.getOsmFeatureType();

        if(featureType == null || featureCollection == null) return null;

        List<SimpleFeature> features = new ArrayList<>();

        int i = 0;
        try (SimpleFeatureIterator featureIterator = featureCollection.features()) {
            while(featureIterator.hasNext()) {
                features.add(
                        layerDataType.toOsmStyleFeature(featureIterator.next(), idGenerator.apply(++i))
                );
            }
        }

        SimpleFeatureCollection newFeatureCollection = new ListFeatureCollection(featureType, features);
        dataStore.dispose();
        return newFeatureCollection;
    }


    @Override
    public String id() {
        return "scjdtogeojsonconverter";
    }
}
