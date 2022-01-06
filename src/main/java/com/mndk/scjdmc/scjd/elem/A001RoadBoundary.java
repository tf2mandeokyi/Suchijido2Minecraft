package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdDefaultElement;
import org.opengis.feature.simple.SimpleFeature;

public class A001RoadBoundary extends ScjdDefaultElement {

    @Column(osmKeyName = "area:highway")
    public final String areaHighway = "road";

    public A001RoadBoundary(SimpleFeature feature) {
        super(feature);
    }

}
