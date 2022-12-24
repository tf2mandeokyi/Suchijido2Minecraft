package com.mndk.scjdmc.column.type;

import com.mndk.scjdmc.column.Column;
import com.mndk.scjdmc.column.ScjdElement;
import com.mndk.scjdmc.column.ScjdElevatedElement;
import lombok.Getter;
import org.opengis.feature.simple.SimpleFeature;

@SuppressWarnings("unused")
public class F001Contour extends ScjdElement implements ScjdElevatedElement {

    @Getter
    @Column(key = "CONT", name = "등고수치", osmName = "elevation")
    public double elevation;

    public F001Contour(SimpleFeature feature) {
        super(feature);
    }
}
