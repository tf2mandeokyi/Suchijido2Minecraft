package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

public class E003River extends ScjdElement {

    @Column(osmKeyName = "natural")
    public final String natural = "water";

    @Column(osmKeyName = "water")
    public final String water = "river";

    public E003River(SimpleFeature feature) {
        super(feature);
    }
}
