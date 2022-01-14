package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

public class A019RailwayPlatform extends ScjdElement {

    @Column(osmKeyName = "railway")
    public final String railway = "platform";

    public A019RailwayPlatform(SimpleFeature feature) {
        super(feature);
    }
}
