package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdDefaultElement;
import org.opengis.feature.simple.SimpleFeature;

public class E008Coastline extends ScjdDefaultElement {

    @Column(jsonName = "natural")
    public final String natural = "coastline";

    public E008Coastline(SimpleFeature feature) {
        super(feature);
    }
}
