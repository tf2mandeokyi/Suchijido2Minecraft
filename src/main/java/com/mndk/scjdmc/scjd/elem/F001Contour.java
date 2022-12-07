package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

public class F001Contour extends ScjdElement {

    @Column(key = "CONT", name = "등고수치", osmName = "elevation")
    public double elevation;

    public F001Contour(SimpleFeature feature) {
        super(feature);
    }
}
