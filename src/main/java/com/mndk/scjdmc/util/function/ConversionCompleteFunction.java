package com.mndk.scjdmc.util.function;

import org.opengis.geometry.BoundingBox;

@FunctionalInterface
public interface ConversionCompleteFunction {
    void accept(String index, BoundingBox bbox);
}
