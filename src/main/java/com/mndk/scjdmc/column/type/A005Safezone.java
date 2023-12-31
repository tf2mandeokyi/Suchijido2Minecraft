package com.mndk.scjdmc.column.type;

import com.mndk.scjdmc.column.Column;
import com.mndk.scjdmc.column.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.coordinate.Polygon;

public class A005Safezone extends ScjdElement {

    @Column(key = "STRU", name = "구조")
    public String type;

    @Column(osmName = "area:highway")
    public String areaHighway;

    @Column(osmName = "highway")
    public String highway;

    @Column(osmName = "crossing:island")
    public String crossingIsland;

    @Column(osmName = "hazard")
    public String hazard;

    public A005Safezone(SimpleFeature feature) {
        super(feature);
        switch(type) {
            case "SZS001": case "교통섬":
                if(feature.getDefaultGeometry() instanceof Polygon) {
                    areaHighway = "traffic_island";
                }
                else {
                    highway = "crossing";
                    crossingIsland = "yes";
                }
                break;
            case "SZS003": case "어린이보호구역":
                hazard = "school_zone";
                break;
        }
    }
}
