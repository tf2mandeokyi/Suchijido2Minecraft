package com.mndk.scjdmc.reader;

import com.mndk.scjdmc.util.ScjdDirectoryParsedMap;
import com.mndk.scjdmc.util.ScjdParsedType;
import com.mndk.scjdmc.util.function.ScjdFeatureCollectionFunction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class GeoJsonDirScjdReader extends ScjdDatasetReader {
    @Override
    public <T> ScjdDirectoryParsedMap<T> read(
            File directory, Charset charset, ScjdParsedType parsedType,
            ScjdFeatureCollectionFunction<T> featureCollectionFunction
    ) throws IOException {
        // TODO
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
