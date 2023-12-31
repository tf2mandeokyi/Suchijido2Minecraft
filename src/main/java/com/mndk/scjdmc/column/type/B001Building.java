package com.mndk.scjdmc.column.type;

import com.mndk.scjdmc.column.Column;
import com.mndk.scjdmc.column.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

@SuppressWarnings("unused")
public class B001Building extends ScjdElement {

    @Column(key = "KIND", name = "종류", osmName = "building")
    public String buildingType;

    @Column(key = "NAME", name = "명칭")
    public String name1;

    @Column(key = "ANNO", name = "주기")
    public String name2;

    @Column(osmName = "name")
    public String name;

    @Column(key = "NMLY", name = "층수", osmName = "building:levels")
    public long buildingLevels;

    public B001Building(SimpleFeature feature) {
        super(feature);
        switch (buildingType) {
            case "BDK001": case "일반주택":
            case "BDK002": case "연립주택": buildingType = "residential"; break;
            case "BDK003": case "아파트": buildingType = "apartments"; break;
            case "BDK004": case "주택외건물":
            case "BDK005": case "무벽건물":
            case "BDK006": case "온실":
            case "BDK007": case "공사중건물":
            case "BDK008": case "가건물":
            case "BDK000": case "미분류":
            default: buildingType = "yes";
        }
        if(name1 != null && !name1.isEmpty()) {
            if(name2 == null || name2.isEmpty()) name = name1;
            else name = name1 + "-" + name2;
        }
        else if(name2 != null && !name2.isEmpty()) name = name2;
    }
}
