package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

public class A009Intersection3d extends ScjdElement {

    @Column(key = "DIVI", name = "구분")
    public String type;

    @Column(osmName = "tunnel")
    public String tunnel;

    @Column(osmName = "man_made")
    public String manMade;

    public A009Intersection3d(SimpleFeature feature) {
        super(feature);
        switch(type) {
            case "OCD001": case "고가차도":
                this.manMade = "bridge";
                break;
            case "OCD002": case "지하차도":
                this.tunnel = "yes";
                break;
        }
    }
}
