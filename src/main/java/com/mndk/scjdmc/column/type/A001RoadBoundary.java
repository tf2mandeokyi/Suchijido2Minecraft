package com.mndk.scjdmc.column.type;

import com.mndk.scjdmc.column.Column;
import com.mndk.scjdmc.column.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

@SuppressWarnings("unused")
public class A001RoadBoundary extends ScjdElement {

    @Column(osmName = "area:highway")
    public final String areaHighway = "road";

    public A001RoadBoundary(SimpleFeature feature) {
        super(feature);
    }

}
