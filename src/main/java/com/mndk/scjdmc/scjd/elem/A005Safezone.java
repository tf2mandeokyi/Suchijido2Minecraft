package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdDefaultElement;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.coordinate.Polygon;

public class A005Safezone extends ScjdDefaultElement {

    @Column(shpColumnName = "구조")
    public String type;

    @Column(osmKeyName = "area:highway")
    public String areaHighway;

    @Column(osmKeyName = "highway")
    public String highway;

    @Column(osmKeyName = "crossing:island")
    public String crossingIsland;

    @Column(osmKeyName = "hazard")
    public String hazard;

    public A005Safezone(SimpleFeature feature) {
        super(feature);
        switch(type) {
            case "교통섬":
                if(feature.getDefaultGeometry() instanceof Polygon) {
                    areaHighway = "traffic_island";
                }
                else {
                    highway = "crossing";
                    crossingIsland = "yes";
                }
                break;
            case "어린이보호구역":
                hazard = "school_zone";
                break;
        }
    }
}
