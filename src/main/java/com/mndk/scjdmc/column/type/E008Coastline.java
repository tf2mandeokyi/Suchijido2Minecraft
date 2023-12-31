package com.mndk.scjdmc.column.type;

import com.mndk.scjdmc.column.Column;
import com.mndk.scjdmc.column.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

@SuppressWarnings("unused")
public class E008Coastline extends ScjdElement {

    @Column(osmName = "natural")
    public final String natural = "coastline";

    public E008Coastline(SimpleFeature feature) {
        super(feature);
    }
}
