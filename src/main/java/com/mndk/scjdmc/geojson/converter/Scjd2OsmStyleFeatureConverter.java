package com.mndk.scjdmc.geojson.converter;

import com.mndk.scjdmc.scjd.LayerDataType;
import com.mndk.scjdmc.scjd.MapIndexManager;
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
import org.geotools.data.store.EmptyFeatureCollection;
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

public class Scjd2OsmStyleFeatureConverter extends ScjdShapefileConverter<SimpleFeatureCollection> implements FileReader {

    public Scjd2OsmStyleFeatureConverter() {
        super();
    }

    @Override
    public SimpleFeatureCollection convert(File source, String charset) throws Exception {
        String parentFileName = source.getParentFile().getName();
        String mapIndex = MapIndexManager.getMapIndexFromFileName(parentFileName);
        return this.convert(mapIndex, source, charset);
    }

    public SimpleFeatureCollection convert(String mapIndex, File source, String charset) throws Exception {

        String layerName = LayerDataType.findLayerTypeString(source.getName());
        LayerDataType layerDataType = LayerDataType.fromLayerName(source.getName());
        SimpleFeatureType featureType = layerDataType.getOsmFeatureType();

        if(featureType == null) return null;
        if(!this.layerFilter.apply(layerDataType)) return null;

        boolean isDbfFileReadable = this.checkIfDbfReadable(source, charset);
        Path dbfFilePath = null, newDbfFilePath = null;
        if(!isDbfFileReadable) {
            // DBF file is corrupted; renaming temporarily so that FeatureSource#getFeatures() won't read it
            String sourcePath = source.getPath();
            dbfFilePath = Paths.get(sourcePath.substring(0, sourcePath.lastIndexOf(".")) + ".dbf");
            newDbfFilePath = Paths.get(sourcePath.substring(0, sourcePath.lastIndexOf(".")) + ".dbf~");
            Files.move(dbfFilePath, newDbfFilePath);
        }

        Map<String, String> params = new HashMap<>();
        params.put("url", source.toURI().toString());
        params.put("charset", charset);
        DataStore dataStore = DataStoreFinder.getDataStore(params);

        String[] typeNames = dataStore.getTypeNames();
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeNames[0]);
        SimpleFeatureCollection featureCollection = featureSource.getFeatures();
        if(featureCollection.isEmpty()) {
            return new EmptyFeatureCollection(featureType);
        }
        featureCollection = applyProjection(mapIndex, featureSource.getFeatures());

        if(!isDbfFileReadable) {
            Files.move(newDbfFilePath, dbfFilePath);
        }

        List<SimpleFeature> features = new ArrayList<>();

        int i = 0;
        try (SimpleFeatureIterator featureIterator = featureCollection.features()) {
            while(featureIterator.hasNext()) {
                features.add(layerDataType.toOsmStyleFeature(
                        featureIterator.next(),
                        mapIndex + "-" + layerName + "-" + (++i)
                ));
            }
        }
        SimpleFeatureCollection newFeatureCollection = new ListFeatureCollection(featureType, features);

        dataStore.dispose();

        return newFeatureCollection;
    }


    private boolean checkIfDbfReadable(File shpSource, String charset) throws IOException {
        ShpFiles shpFiles = new ShpFiles(shpSource);
        try(ReadableByteChannel dbfChannel = shpFiles.getReadChannel(ShpFileType.DBF, this)) {
            DbaseFileHeader dbfHeader = new DbaseFileHeader(Charset.forName(charset));
            dbfHeader.readHeader(dbfChannel);
        } catch(IllegalArgumentException e) {
            return false;
        }
        return true;
    }


    private static SimpleFeatureCollection applyProjection(String mapIndex, SimpleFeatureCollection featureCollection) {

        SimpleFeatureCollection result;

        // CRS
        CoordinateReferenceSystem indexCrs = MapIndexManager.getCoordinateReferenceSystemFromIndex(mapIndex);
        CoordinateReferenceSystem sourceCrs = featureCollection.getSchema().getGeometryDescriptor()
                .getCoordinateReferenceSystem();

        // Expected bounding box
        BoundingBox bbox = MapIndexManager.getBoudingBox(mapIndex, true);

        // Try #1: Apply with .prj file
        if(sourceCrs != null) {
            result = new ReprojectingFeatureCollection(featureCollection, sourceCrs, CRS84);
            if(result.getBounds().intersects(bbox)) return result;

            // Try #1-1: Apply with .prj file with xy coordinates swapped
            result = new ReprojectingFeatureCollection(
                    new ReprojectingFeatureCollection(featureCollection, EPSG4326, CRS84),
                    sourceCrs, CRS84
            );
            if(result.getBounds().intersects(bbox)) return result;
        }

        // Try #2: Use CRS from index name
        if(indexCrs == null) throw new RuntimeException("Illegal index");
        result = new ReprojectingFeatureCollection(featureCollection, indexCrs, CRS84);
        if(result.getBounds().intersects(bbox)) return result;

        // Try #2-1: Use CRS from index name with xy coordinates swapped
        result = new ReprojectingFeatureCollection(
                new ReprojectingFeatureCollection(featureCollection, EPSG4326, CRS84),
                indexCrs, CRS84
        );
        if(result.getBounds().intersects(bbox)) return result;

        throw new RuntimeException("Failed to analyze projection; [" + mapIndex + "] : " + result.getBounds() + ", " + bbox);
    }

    @Override
    public String id() {
        return "shptogeojsonconverter";
    }
}
