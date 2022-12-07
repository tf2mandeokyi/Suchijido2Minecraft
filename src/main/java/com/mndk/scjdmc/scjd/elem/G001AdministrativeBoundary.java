package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

public class G001AdministrativeBoundary extends ScjdElement {

    @Column(key = "NAME", name = "명칭", osmName = "name")
    public String name;

    @Column(key = "DIVI", name = "구분")
    public String type;

    @Column(osmName = "admin_level")
    public final int adminLevel;

    public G001AdministrativeBoundary(SimpleFeature feature) {
        super(feature);
        switch(type) {
            case "":
            case "HJD001": case "국경":
                adminLevel = 2; break;
            case "HJD002": case "특별시":
            case "HJD003": case "광역시":
            case "HJD004": case "도":
                adminLevel = 4; break;
            case "HJD005": case "시":
            case "HJD006": case "군":
                adminLevel = 6; break;
            case "HJD007": case "구":
                adminLevel = 7; break;
            case "HJD008": case "읍":
            case "HJD009": case "면":
            case "HJD010": case "법정동":
            case "HJD011": case "행정동":
                adminLevel = 8; break;
            case "HJD012": case "리":
            default:
                adminLevel = 10; break;
        }
    }
}
