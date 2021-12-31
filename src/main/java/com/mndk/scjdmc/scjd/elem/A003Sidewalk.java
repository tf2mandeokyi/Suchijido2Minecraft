package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdDefaultElement;
import org.opengis.feature.simple.SimpleFeature;

public class A003Sidewalk extends ScjdDefaultElement {

    @Column(jsonName = "area:highway")
    public final String areaHighway = "footway";

    @Column(jsonName = "highway")
    public final String highway = "pedestrian";

    @Column(name = "자전거도로유무", jsonName = "bicycle")
    public String bicycle;

    public A003Sidewalk(SimpleFeature feature) {
        super(feature);
        this.bicycle = "유".equals(this.bicycle) ? "yes" : null;
    }
}
