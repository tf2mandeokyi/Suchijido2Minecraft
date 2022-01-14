package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

public class A009Intersection3d extends ScjdElement {

    @Column(shpColumnName = "구분")
    public String type;

    @Column(osmKeyName = "tunnel")
    public String tunnel;

    @Column(osmKeyName = "man_made")
    public String manMade;

    public A009Intersection3d(SimpleFeature feature) {
        super(feature);
        switch(type) {
            case "지하차도":
                this.tunnel = "yes";
                break;
            case "고가도로":
                this.manMade = "bridge";
                break;
        }
    }
}
