package com.mndk.scjdmc.column.type;

import com.mndk.scjdmc.column.Column;
import com.mndk.scjdmc.column.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

@SuppressWarnings("unused")
public class A016RailwayBoundary extends ScjdElement {

    @Column(osmName = "landuse")
    public final String landuse = "railway";

    public A016RailwayBoundary(SimpleFeature feature) {
        super(feature);
    }
}
