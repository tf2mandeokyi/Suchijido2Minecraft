package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

public class E003River extends ScjdElement {

    @Column(osmName = "natural")
    public final String natural = "water";

    @Column(osmName = "water")
    public final String water = "river";

    public E003River(SimpleFeature feature) {
        super(feature);
    }
}
