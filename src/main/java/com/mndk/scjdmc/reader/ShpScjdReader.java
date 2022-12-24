package com.mndk.scjdmc.reader;

import com.mndk.scjdmc.Constants;
import com.mndk.scjdmc.column.LayerDataType;
import com.mndk.scjdmc.util.file.ScjdFileInformation;
import com.mndk.scjdmc.util.ScjdMapIndexUtils;
import com.mndk.scjdmc.util.ScjdParsedType;
import com.mndk.scjdmc.util.function.ThrowableRunner;
import com.mndk.scjdmc.util.function.ScjdFeatureCollectionFunction;
import org.apache.commons.io.FilenameUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.files.FileReader;
import org.geotools.data.shapefile.files.ShpFileType;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.store.ReprojectingFeatureCollection;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.File;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ShpScjdReader {


    private static final FileReader REQUESTOR = () -> "shpscjdreader";


    public static <T> T read(
            File shpFile, ScjdFileInformation fileInformation, Charset charset,
            ScjdFeatureCollectionFunction<T> featureCollectionFunction
    ) throws IOException {

        AtomicReference<DataStore> dataStore = new AtomicReference<>();
        AtomicReference<SimpleFeatureCollection> featureCollection = new AtomicReference<>();

        handleDbfCorruption(shpFile, charset, () -> {
            Map<String, String> params = new HashMap<>();
            params.put("url", shpFile.toURI().toString());
            params.put("charset", charset.name());
            dataStore.set(DataStoreFinder.getDataStore(params));

            String[] typeNames = dataStore.get().getTypeNames();
            SimpleFeatureSource featureSource = dataStore.get().getFeatureSource(typeNames[0]);
            SimpleFeatureCollection result = featureSource.getFeatures();

            if(!result.isEmpty()) {
                if(fileInformation.getParsedType() == ScjdParsedType.INDEX)
                    featureCollection.set(applyIndexProjection(fileInformation.getNameOrIndex(), featureSource.getFeatures()));
                else
                    featureCollection.set(applyNormalProjection(featureSource.getFeatures()));
            }
        });

        LayerDataType layerDataType = LayerDataType.fromLayerName(shpFile.getName());
        T result = featureCollectionFunction.apply(featureCollection.get(), layerDataType);
        dataStore.get().dispose();
        return result;

    }


    private static boolean checkDbfReadable(File shpFile, Charset charset) throws IOException {
        ShpFiles shpFiles = new ShpFiles(shpFile);
        try(ReadableByteChannel dbfChannel = shpFiles.getReadChannel(ShpFileType.DBF, REQUESTOR)) {
            DbaseFileHeader dbfHeader = new DbaseFileHeader(charset);
            dbfHeader.readHeader(dbfChannel);
        } catch(IllegalArgumentException e) {
            return false;
        }
        return true;
    }


    private static void handleDbfCorruption(
            File shpFile, Charset charset, ThrowableRunner<IOException> callback
    ) throws IOException {
        boolean isDbfFileReadable = checkDbfReadable(shpFile, charset);
        Path dbfFilePath = null, newDbfFilePath = null;

        if(!isDbfFileReadable) {
            // DBF file is corrupted; renaming temporarily so that FeatureSource#getFeatures() won't read it
            String parentPath = shpFile.getParentFile().getAbsolutePath();
            String shpBasename = FilenameUtils.getBaseName(shpFile.getName());
            dbfFilePath = Paths.get(parentPath, shpBasename + ".dbf");
            newDbfFilePath = Paths.get(parentPath, shpBasename + ".dbf~");
            Files.move(dbfFilePath, newDbfFilePath);
        }

        callback.run();

        if(!isDbfFileReadable) {
            Files.move(newDbfFilePath, dbfFilePath);
        }
    }


    private static SimpleFeatureCollection applyNormalProjection(
            SimpleFeatureCollection featureCollection
    ) {

        // CRS
        CoordinateReferenceSystem sourceCrs = featureCollection.getSchema().getGeometryDescriptor()
                .getCoordinateReferenceSystem();

        // Try #1: Apply with .prj file
        if(sourceCrs != null) {
            return new ReprojectingFeatureCollection(featureCollection, sourceCrs, Constants.CRS84);
        }

        throw new RuntimeException(".prj file not present");
    }


    private static SimpleFeatureCollection applyIndexProjection(
            String mapIndex, SimpleFeatureCollection featureCollection
    ) {

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

}
