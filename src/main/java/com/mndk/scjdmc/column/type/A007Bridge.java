package com.mndk.scjdmc.column.type;

import com.mndk.scjdmc.column.Column;
import com.mndk.scjdmc.column.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

@SuppressWarnings("unused")
public class A007Bridge extends ScjdElement {

    @Column(osmName = "man_made")
    public final String manMade = "bridge";

    @Column(key = "KIND", name = "종류")
    public String type;

    @Column(osmName = "highway")
    public String highway;

    @Column(osmName = "railway")
    public String railway;

    @Column(osmName = "layer")
    public final int layer = 1;

    public A007Bridge(SimpleFeature feature) {
        super(feature);
        if(type != null) switch(type) {
            case "BRK001": case "도로교": highway = "road"; break;
            case "BRK002": case "보도교": highway = "footway"; break;
            case "BRK003": case "철교": railway = "rail"; break;
        }
    }
}
