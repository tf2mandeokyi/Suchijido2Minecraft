package com.mndk.scjdmc.column.type;

import com.mndk.scjdmc.column.Column;
import com.mndk.scjdmc.column.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

@SuppressWarnings("unused")
public class C001Dam extends ScjdElement {

    @Column(osmName = "waterway")
    public final String waterway = "dam";

    public C001Dam(SimpleFeature feature) {
        super(feature);
    }
}
