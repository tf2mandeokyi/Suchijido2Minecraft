package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdDefaultElement;
import org.opengis.feature.simple.SimpleFeature;

public class E005Lake extends ScjdDefaultElement {

    @Column(jsonName = "natural")
    public final String natural = "water";

    @Column(jsonName = "water")
    public final String water = "lake";

    @Column(name = "명칭", jsonName = "name")
    public String name;

    public E005Lake(SimpleFeature feature) {
        super(feature);
    }
}
