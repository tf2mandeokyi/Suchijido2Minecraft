package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

public class A016RailwayBoundary extends ScjdElement {

    @Column(osmKeyName = "landuse")
    public final String landuse = "railway";

    public A016RailwayBoundary(SimpleFeature feature) {
        super(feature);
    }
}
