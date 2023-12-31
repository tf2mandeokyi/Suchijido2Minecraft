package com.mndk.scjdmc.column.type;

import com.mndk.scjdmc.column.Column;
import com.mndk.scjdmc.column.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

@SuppressWarnings("unused")
public class E003River extends ScjdElement {

    @Column(osmName = "natural")
    public final String natural = "water";

    @Column(osmName = "water")
    public final String water = "river";

    public E003River(SimpleFeature feature) {
        super(feature);
    }
}
