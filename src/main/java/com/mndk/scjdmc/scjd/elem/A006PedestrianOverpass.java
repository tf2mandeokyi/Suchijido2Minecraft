package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import org.opengis.feature.simple.SimpleFeature;

public class A006PedestrianOverpass extends A007Bridge {

    @Column(jsonName = "highway")
    public final String highway = "footway";

    public A006PedestrianOverpass(SimpleFeature feature) {
        super(feature);
    }
}
