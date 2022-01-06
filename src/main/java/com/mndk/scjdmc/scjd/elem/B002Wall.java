package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdDefaultElement;
import org.opengis.feature.simple.SimpleFeature;

public class B002Wall extends ScjdDefaultElement {

    @Column(osmKeyName = "barrier")
    public final String barrier = "wall";

    public B002Wall(SimpleFeature feature) {
        super(feature);
    }
}
