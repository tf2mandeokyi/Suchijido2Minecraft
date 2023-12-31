package com.mndk.scjdmc.column.type;

import com.mndk.scjdmc.column.Column;
import com.mndk.scjdmc.column.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

@SuppressWarnings("unused")
public class A002RoadCenterline extends ScjdElement {

    @Column(key = "RDDV", name = "도로구분", osmName = "highway")
    public String highwayType;

    @Column(key = "RDLN", name = "차로수", osmName = "lanes")
    public long lanes;

    @Column(key = "RVWD", name = "도로폭", osmName = "width")
    public double width;

    @Column(key = "ONSD", name = "일방통행", osmName = "oneway")
    public String oneWay;

    public A002RoadCenterline(SimpleFeature feature) {
        super(feature);
        switch (highwayType) {
            case "RDD001": case "고속국도": highwayType = "motorway"; break;
            case "RDD002": case "일반국도": highwayType = "trunk"; break;
            case "RDD003": case "지방도": highwayType = "primary"; break;
            case "RDD004": case "특별시도":
            case "RDD005": case "광역시도":
            case "RDD006": case "시도": highwayType = "secondary"; break;
            case "RDD007": case "군도":
            case "RDD008": case "면리간도로": highwayType = "tertiary"; break;
            case "RDD009": case "소로": highwayType = "residental"; break;
            case "RDD000": case "미분류":
            default: highwayType = "road";
        }
        oneWay = "일방통행".equals(oneWay) ? "yes" : null;
    }
}
