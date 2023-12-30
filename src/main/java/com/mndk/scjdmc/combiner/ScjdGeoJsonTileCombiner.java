package com.mndk.scjdmc.combiner;

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
import java.nio.charset.Charset;

public class ScjdGeoJsonTileCombiner {

    public static int combine(File sourceFolder, Charset sourceEncoding,
                              File destinationFolder, Charset destinationEncoding,
                              TppTileCoordinate coordinate, GeoJsonDirScjdReader reader) throws IOException {

        File coordinateFolder = coordinate.getFolderLocation(sourceFolder, false);
        ScjdDirectoryParsedMap<SimpleFeatureCollection> featureMap = Scjd2OsmFeatureConverter.parseAsOsmFeature(
                coordinateFolder, sourceEncoding, ScjdParsedType.TILE, reader, feature -> true
        );
        featureMap = ScjdOsmFeatureScissor.apply(featureMap, coordinate.getTileGeometry(0.1));

        File jsonLocation = coordinate.getJsonLocation(destinationFolder, true);
        ScjdGeoJsonWriter.writeAsSingleJsonFile(featureMap, jsonLocation, destinationEncoding);
        return featureMap.size();
    }

}
