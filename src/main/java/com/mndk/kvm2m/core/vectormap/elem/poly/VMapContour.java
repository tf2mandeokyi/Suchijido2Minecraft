package com.mndk.kvm2m.core.vectormap.elem.poly;

import com.mndk.kvm2m.core.projection.Grs80Projection;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vectormap.elem.IHasElevationData;
import com.mndk.kvm2m.core.vectormap.elem.VMapElementLayer;
import com.mndk.ngiparser.ngi.element.NgiLineElement;

public class VMapContour extends VMapPolyline implements IHasElevationData {

	/*
	public VMapContour(DXFLWPolyline polyline, Grs80Projection projection) {
		super(polyline, projection);
		this.elevation = (int) polyline.getElevation();
	}
	*/

	public VMapContour(VMapElementLayer parent, NgiLineElement polyline, int elevation, Grs80Projection projection) {
		super(parent, polyline, null, elevation, projection);
	}

	public VMapContour(VMapElementLayer parent, Vector2DH[] vertexes, int elevation) {
		super(parent, vertexes, null, elevation, false);
	}
	
	@Override
	public int getElevation() {
		return this.y;
	}
}
