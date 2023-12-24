package com.mndk.scjdmc.reader;

import com.mndk.scjdmc.util.ScjdDirectoryParsedMap;
import com.mndk.scjdmc.util.ScjdParsedType;
import com.mndk.scjdmc.util.function.LayerFilterFunction;
import com.mndk.scjdmc.util.function.ScjdFeatureCollectionFunction;
import jakarta.annotation.Nullable;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

@Setter
public abstract class ScjdDatasetReader {
    
    protected LayerFilterFunction layerFilter = f -> true;

    public abstract <T> ScjdDirectoryParsedMap<T> read(
            File directory, Charset charset, ScjdParsedType parsedType,
            ScjdFeatureCollectionFunction<T> featureCollectionFunction
    ) throws IOException;

    @Nullable
    public static ScjdDatasetReader getShpReader(File file) {
        String sourceFileName = file.getName();
        String sourceExtension = FilenameUtils.getExtension(sourceFileName).toLowerCase(Locale.ROOT);

        if(file.isDirectory())                   return new ShpDirScjdReader();
        else if ("zip".equals(sourceExtension))  return new ZipScjdReader();
        else return null;
    }
    
}
