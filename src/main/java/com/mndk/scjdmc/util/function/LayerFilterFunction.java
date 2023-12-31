package com.mndk.scjdmc.util.function;

import com.mndk.scjdmc.column.LayerDataType;

@FunctionalInterface
public interface LayerFilterFunction {
    boolean apply(LayerDataType type);
}
