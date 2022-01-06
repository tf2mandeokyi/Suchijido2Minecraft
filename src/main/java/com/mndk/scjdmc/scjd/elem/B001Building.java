package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdDefaultElement;
import org.opengis.feature.simple.SimpleFeature;

public class B001Building extends ScjdDefaultElement {

    @Column(shpColumnName = "종류", osmKeyName = "building")
    public String buildingType;

    @Column(shpColumnName = "명칭")
    public String name1;

    @Column(shpColumnName = "주기")
    public String name2;

    @Column(osmKeyName = "name")
    public String name;

    @Column(shpColumnName = "층수", osmKeyName = "building:levels")
    public long buildingLevels;

    public B001Building(SimpleFeature feature) {
        super(feature);
        switch (buildingType) {
            case "일반주택":
            case "연립주택": buildingType = "residential"; break;
            case "아파트": buildingType = "apartments"; break;
            case "주택외건물":
            case "무벽건물":
            case "온실":
            case "공사중건물":
            case "가건물":
            case "미분류":
            default: buildingType = "yes";
        }
        if(name1 != null && name1.length() != 0) {
            if(name2 == null || name2.length() == 0) name = name1;
            else name = name1 + "-" + name2;
        }
        else if(name2 != null && name2.length() != 0) name = name2;
    }
}
