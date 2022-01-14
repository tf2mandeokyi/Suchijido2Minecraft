package com.mndk.scjdmc.scjd.elem;

import com.mndk.scjdmc.scjd.Column;
import com.mndk.scjdmc.scjd.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

public class G001AdministrativeBoundary extends ScjdElement {

    @Column(shpColumnName = "명칭", osmKeyName = "name")
    public String name;

    @Column(shpColumnName = "구분")
    public String type;

    @Column(osmKeyName = "admin_level")
    public final int adminLevel;

    public G001AdministrativeBoundary(SimpleFeature feature) {
        super(feature);
        switch(type) {
            case "국경":
                adminLevel = 2; break;
            case "특별시": case "광역시": case "도":
                adminLevel = 4; break;
            case "시": case "군":
                adminLevel = 6; break;
            case "구":
                adminLevel = 7; break;
            case "읍": case "면": case "법정동": case "행정동":
                adminLevel = 8; break;
            case "리": default:
                adminLevel = 10; break;
        }
    }
}
