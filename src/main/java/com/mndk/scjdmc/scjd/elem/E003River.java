package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdDefaultElement;
import org.opengis.feature.simple.SimpleFeature;

public class E003River extends ScjdDefaultElement {

    @Column(jsonName = "natural")
    public final String natural = "water";

    @Column(jsonName = "water")
    public final String water = "river";

    public E003River(SimpleFeature feature) {
        super(feature);
    }
}
