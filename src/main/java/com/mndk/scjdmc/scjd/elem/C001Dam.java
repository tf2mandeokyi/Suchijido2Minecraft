package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

public class C001Dam extends ScjdElement {

    @Column(osmKeyName = "waterway")
    public final String waterway = "dam";

    public C001Dam(SimpleFeature feature) {
        super(feature);
    }
}
