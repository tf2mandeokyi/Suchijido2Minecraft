package com.mndk.scjdmc.util.function;

import com.mndk.scjdmc.scjd.LayerDataType;

@FunctionalInterface
public interface LayerFilterFunction {

    LayerFilterFunction DEFAULT_FILTER = (type) -> true;

    boolean apply(LayerDataType type);

}
