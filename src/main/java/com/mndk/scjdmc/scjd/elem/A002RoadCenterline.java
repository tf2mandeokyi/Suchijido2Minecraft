package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdDefaultElement;
import org.opengis.feature.simple.SimpleFeature;

public class A002RoadCenterline extends ScjdDefaultElement {

    @Column(name = "도로구분", jsonName = "highway")
    public String highwayType;

    @Column(name = "차로수", jsonName = "lanes")
    public long lanes;

    @Column(name = "도로폭", jsonName = "width")
    public double width;

    @Column(name = "일방통행", jsonName = "oneway")
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
