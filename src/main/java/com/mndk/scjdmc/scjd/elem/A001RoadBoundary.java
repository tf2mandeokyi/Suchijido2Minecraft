package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

public class A001RoadBoundary extends ScjdElement {

    @Column(osmKeyName = "area:highway")
    public final String areaHighway = "road";

    public A001RoadBoundary(SimpleFeature feature) {
        super(feature);
    }

}
