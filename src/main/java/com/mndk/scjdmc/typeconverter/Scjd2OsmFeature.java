package com.mndk.scjdmc.typeconverter;

import com.mndk.scjdmc.util.ParsedOsmFeatureMap;
import com.mndk.scjdmc.util.ScjdParsedType;
import com.mndk.scjdmc.util.function.LayerFilterFunction;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class Scjd2OsmFeature {

    public static ParsedOsmFeatureMap parseAsOsmFeature(
            File file, String charset, ScjdParsedType parsedType, LayerFilterFunction layerFilter
    ) throws IOException {
        String sourceFileName = file.getName();
        String sourceExtension = FilenameUtils.getExtension(sourceFileName);

        switch(sourceExtension.toLowerCase(Locale.ROOT)) {
            case "zip": return new ZipScjd2OsmFeature(layerFilter).convert(file, charset, parsedType);
            default: throw new IOException("Unsupported extension: " + sourceExtension);
        }
    }

    public static ParsedOsmFeatureMap parseAsOsmFeature(
            File file, String charset, ScjdParsedType parsedType
    ) throws IOException {
        return parseAsOsmFeature(file, charset, parsedType, f -> true);
    }
}
