package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdDefaultElement;
import org.opengis.feature.simple.SimpleFeature;

public class F002ElevationPoint extends ScjdDefaultElement {

    @Column(shpColumnName = "수치", osmKeyName = "elevation")
    public double elevation;

    public F002ElevationPoint(SimpleFeature feature) {
        super(feature);
    }
}
