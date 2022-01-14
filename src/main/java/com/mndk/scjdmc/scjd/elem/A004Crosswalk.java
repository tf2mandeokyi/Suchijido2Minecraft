package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

public class A004Crosswalk extends ScjdElement {

    @Column(osmKeyName = "highway")
    public final String highway = "crossing";

    public A004Crosswalk(SimpleFeature feature) {
        super(feature);
    }
}
