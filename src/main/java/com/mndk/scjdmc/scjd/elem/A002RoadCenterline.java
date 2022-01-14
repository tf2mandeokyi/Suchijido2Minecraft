package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

public class A002RoadCenterline extends ScjdElement {

    @Column(shpColumnName = "도로구분", osmKeyName = "highway")
    public String highwayType;

    @Column(shpColumnName = "차로수", osmKeyName = "lanes")
    public long lanes;

    @Column(shpColumnName = "도로폭", osmKeyName = "width")
    public double width;

    @Column(shpColumnName = "일방통행", osmKeyName = "oneway")
    public String oneWay;

    public A002RoadCenterline(SimpleFeature feature) {
        super(feature);
        switch (highwayType) {
            case "고속국도": highwayType = "motorway"; break;
            case "일반국도": highwayType = "trunk"; break;
            case "지방도": highwayType = "primary"; break;
            case "특별시도":
            case "광역시도":
            case "시도": highwayType = "secondary"; break;
            case "군도":
            case "면리간도로": highwayType = "tertiary"; break;
            case "소로": highwayType = "residental"; break;
            case "미분류":
            default: highwayType = "unclassified";
        }
        oneWay = "일방통행".equals(oneWay) ? "yes" : null;
    }
}
