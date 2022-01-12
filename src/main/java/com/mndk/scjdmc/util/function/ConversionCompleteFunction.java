package com.mndk.scjdmc.util.function;

import org.opengis.geometry.BoundingBox;

import java.io.File;

@FunctionalInterface
public interface ConversionCompleteFunction {
    void accept(File file, BoundingBox bbox);
}
