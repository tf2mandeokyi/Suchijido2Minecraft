package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

public class A007Bridge extends ScjdElement {

    @Column(osmKeyName = "man_made")
    public final String manMade = "bridge";

    @Column(shpColumnName = "종류")
    public String type;

    @Column(osmKeyName = "highway")
    public String highway;

    @Column(osmKeyName = "railway")
    public String railway;

    @Column(osmKeyName = "layer")
    public final int layer = 1;

    public A007Bridge(SimpleFeature feature) {
        super(feature);
        if(type != null) switch(type) {
            case "도로교": highway = "road"; break;
            case "보도교": highway = "footway"; break;
            case "철교": railway = "rail"; break;
        }
    }
}
