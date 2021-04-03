package com.mndk.kvm2m.core.vectormap.elem.poly;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vectormap.VMapBlockSelector;
import com.mndk.kvm2m.core.vectormap.elem.VMapElementLayer;

public class VMapContour extends VMapPolyline {

	public final int elevation;
	
	public VMapContour(VMapElementLayer parent, Vector2DH[] vertexes, Object[] rowData) {
		super(parent, vertexes, rowData, false);
		this.elevation = VMapBlockSelector.getAdditionalHeight(this);
	}
}
