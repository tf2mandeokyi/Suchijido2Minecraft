package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

public class A007Bridge extends ScjdElement {

    @Column(osmKeyName = "man_made")
    public final String manMade = "bridge";

    @Column(osmKeyName = "layer")
    public final int layer = 1;

    public A007Bridge(SimpleFeature feature) {
        super(feature);
    }
}
