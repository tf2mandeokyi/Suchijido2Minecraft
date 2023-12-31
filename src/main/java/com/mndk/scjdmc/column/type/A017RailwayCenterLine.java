package com.mndk.scjdmc.column.type;

import com.mndk.scjdmc.column.Column;
import com.mndk.scjdmc.column.ScjdElement;
import org.opengis.feature.simple.SimpleFeature;

@SuppressWarnings("unused")
public class A017RailwayCenterLine extends ScjdElement {

    @Column(osmName = "railway")
    public final String railway = "rail";

    @Column(key = "NAME", name = "명칭", osmName = "name")
    public String name;

    public A017RailwayCenterLine(SimpleFeature feature) {
        super(feature);
    }
}
