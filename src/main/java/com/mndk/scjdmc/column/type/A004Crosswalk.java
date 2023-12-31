package com.mndk.scjdmc.column.type;

import com.mndk.scjdmc.column.Column;
import com.mndk.scjdmc.column.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

@SuppressWarnings("unused")
public class A004Crosswalk extends ScjdElement {

    @Column(osmName = "highway")
    public final String highway = "crossing";

    public A004Crosswalk(SimpleFeature feature) {
        super(feature);
    }
}
