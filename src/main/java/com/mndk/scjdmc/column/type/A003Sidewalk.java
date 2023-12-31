package com.mndk.scjdmc.column.type;

import com.mndk.scjdmc.column.Column;
import com.mndk.scjdmc.column.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

@SuppressWarnings("unused")
public class A003Sidewalk extends ScjdElement {

    @Column(osmName = "area:highway")
    public final String areaHighway = "footway";

    @Column(osmName = "highway")
    public final String highway = "pedestrian";

    @Column(key = "BYYN", name = "자전거도로유무", osmName = "bicycle")
    public String bicycle;

    public A003Sidewalk(SimpleFeature feature) {
        super(feature);
        this.bicycle = ( "유".equals(this.bicycle) || "BYC001".equals(this.bicycle) ) ?
                "yes" : null;
    }
}
