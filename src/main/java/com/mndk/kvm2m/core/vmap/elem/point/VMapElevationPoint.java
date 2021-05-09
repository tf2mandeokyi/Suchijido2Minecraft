package com.mndk.kvm2m.core.vmap.elem.point;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vmap.VMapElementStyleSelector;
import com.mndk.kvm2m.core.vmap.elem.VMapElementLayer;

public class VMapElevationPoint extends VMapPoint {
	
	public final int y;
	
	public VMapElevationPoint(VMapElementLayer layer, Vector2DH point, Object[] rowData) {
		super(layer, point, rowData);
		this.y = VMapElementStyleSelector.getStyle(this)[0].y;
	}
	
	public Vector2DH toVector() {
		return this.getPosition().withHeight(this.y);
	}

}
