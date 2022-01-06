package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdDefaultElement;
import org.opengis.feature.simple.SimpleFeature;

public class F001Contour extends ScjdDefaultElement {

    @Column(shpColumnName = "등고수치", osmKeyName = "elevation")
    public double elevation;

    public F001Contour(SimpleFeature feature) {
        super(feature);
    }
}
