package com.mndk.kvm2m.core.vectormap.elem.point;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vectormap.VMapBlockSelector;
import com.mndk.kvm2m.core.vectormap.elem.VMapElementLayer;

public class VMapElevationPoint extends VMapPoint {
	
	public final int y;
	
	public VMapElevationPoint(VMapElementLayer layer, Vector2DH point, Object[] rowData) {
		super(layer, point, rowData);
		this.y = VMapBlockSelector.getAdditionalHeight(this);
	}
	
	public Vector2DH toVector() {
		return this.getPosition().withHeight(this.y);
	}

}
