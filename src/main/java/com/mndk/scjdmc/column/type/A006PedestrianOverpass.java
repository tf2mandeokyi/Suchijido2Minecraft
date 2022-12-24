package com.mndk.scjdmc.column.type;

import com.mndk.scjdmc.column.Column;
import org.opengis.feature.simple.SimpleFeature;

@SuppressWarnings("unused")
public class A006PedestrianOverpass extends A007Bridge {

    @Column(osmName = "highway")
    public final String highway = "footway";

    public A006PedestrianOverpass(SimpleFeature feature) {
        super(feature);
    }
}
