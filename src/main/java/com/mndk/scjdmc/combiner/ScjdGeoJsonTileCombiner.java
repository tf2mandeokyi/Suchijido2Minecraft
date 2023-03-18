package com.mndk.scjdmc.combiner;

import com.mndk.scjdmc.Constants;
import com.mndk.scjdmc.reader.GeoJsonDirScjdReader;
import com.mndk.scjdmc.scissor.ScjdOsmFeatureScissor;
import com.mndk.scjdmc.typeconverter.Scjd2OsmFeatureConverter;
import com.mndk.scjdmc.util.ScjdDirectoryParsedMap;
import com.mndk.scjdmc.util.ScjdParsedType;
import com.mndk.scjdmc.util.TppTileCoordinate;
import com.mndk.scjdmc.writer.ScjdGeoJsonWriter;
import org.geotools.data.simple.SimpleFeatureCollection;

import java.io.File;
import java.io.IOException;

public class ScjdGeoJsonTileCombiner {

    public static void combine(
            File sourceFolder, File destinationFolder, TppTileCoordinate coordinate, GeoJsonDirScjdReader reader
    ) throws IOException {

        File coordinateFolder = coordinate.getFolderLocation(sourceFolder, false);
        ScjdDirectoryParsedMap<SimpleFeatureCollection> featureMap = Scjd2OsmFeatureConverter.parseAsOsmFeature(
                coordinateFolder, Constants.CP949, ScjdParsedType.TILE, reader, feature -> true
        );
        featureMap = ScjdOsmFeatureScissor.apply(featureMap, coordinate.getTileGeometry(0.1));

        ScjdGeoJsonWriter.writeAsSingleJsonFile(
                featureMap,
                coordinate.getJsonLocation(destinationFolder, true)
        );

    }

}
