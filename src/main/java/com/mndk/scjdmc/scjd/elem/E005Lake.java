package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

public class E005Lake extends ScjdElement {

    @Column(osmName = "natural")
    public final String natural = "water";

    @Column(osmName = "water")
    public final String water = "lake";

    @Column(key = "NAME", name = "명칭", osmName = "name")
    public String name;

    public E005Lake(SimpleFeature feature) {
        super(feature);
    }
}
