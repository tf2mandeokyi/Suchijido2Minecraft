package com.mndk.scjdmc.util.function;

import org.opengis.feature.simple.SimpleFeature;

import java.util.function.Function;

public interface FeatureFilter extends Function<SimpleFeature, Boolean> {
}
