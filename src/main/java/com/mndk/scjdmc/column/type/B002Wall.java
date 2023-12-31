package com.mndk.scjdmc.column.type;

import com.mndk.scjdmc.column.Column;
import com.mndk.scjdmc.column.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

@SuppressWarnings("unused")
public class B002Wall extends ScjdElement {

    @Column(osmName = "barrier")
    public final String barrier = "wall";

    public B002Wall(SimpleFeature feature) {
        super(feature);
    }
}
