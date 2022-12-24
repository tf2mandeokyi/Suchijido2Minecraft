package com.mndk.scjdmc.column.type;

import com.mndk.scjdmc.column.Column;
import com.mndk.scjdmc.column.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

@SuppressWarnings("unused")
public class A019RailwayPlatform extends ScjdElement {

    @Column(osmName = "railway")
    public final String railway = "platform";

    public A019RailwayPlatform(SimpleFeature feature) {
        super(feature);
    }
}
