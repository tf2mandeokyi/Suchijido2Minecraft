package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

public class A011Tunnel extends ScjdElement {

    @Column(osmKeyName = "tunnel")
    public final String tunnel = "yes";


    public A011Tunnel(SimpleFeature feature) {
        super(feature);
    }
}
