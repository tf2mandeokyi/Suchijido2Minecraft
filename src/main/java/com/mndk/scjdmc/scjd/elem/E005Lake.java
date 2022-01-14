package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

public class E005Lake extends ScjdElement {

    @Column(osmKeyName = "natural")
    public final String natural = "water";

    @Column(osmKeyName = "water")
    public final String water = "lake";

    @Column(shpColumnName = "명칭", osmKeyName = "name")
    public String name;

    public E005Lake(SimpleFeature feature) {
        super(feature);
    }
}
