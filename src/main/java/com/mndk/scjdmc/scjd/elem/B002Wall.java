package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

public class B002Wall extends ScjdElement {

    @Column(osmName = "barrier")
    public final String barrier = "wall";

    public B002Wall(SimpleFeature feature) {
        super(feature);
    }
}
