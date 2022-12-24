package com.mndk.scjdmc.column.type;

import com.mndk.scjdmc.column.Column;
import com.mndk.scjdmc.column.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

@SuppressWarnings("unused")
public class A011Tunnel extends ScjdElement {

    @Column(osmName = "tunnel")
    public final String tunnel = "yes";

    public A011Tunnel(SimpleFeature feature) {
        super(feature);
    }
}
