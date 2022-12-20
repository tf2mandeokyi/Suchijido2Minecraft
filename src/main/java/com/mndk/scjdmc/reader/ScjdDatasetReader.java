package com.mndk.scjdmc.reader;

import com.mndk.scjdmc.util.ScjdDirectoryParsedMap;
import com.mndk.scjdmc.util.ScjdParsedType;
import com.mndk.scjdmc.util.function.LayerFilterFunction;
import com.mndk.scjdmc.util.function.ScjdFeatureCollectionFunction;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

public abstract class ScjdDatasetReader {
    
    @Setter
    protected LayerFilterFunction layerFilter = f -> true;

    public abstract <T> ScjdDirectoryParsedMap<T> read(
            File directory, Charset charset, ScjdParsedType parsedType,
            ScjdFeatureCollectionFunction<T> featureCollectionFunction
    ) throws IOException;


    public static ScjdDatasetReader getShpReader(File file) throws IOException {
        String sourceFileName = file.getName();
        String sourceExtension = FilenameUtils.getExtension(sourceFileName).toLowerCase(Locale.ROOT);

        if(file.isDirectory())                   return new ShpDirScjdReader();
        else if ("zip".equals(sourceExtension))  return new ZipScjdReader();
        else throw new IOException("Unsupported extension: " + sourceExtension);
    }
    
}
