package com.mndk.kvm2m.core.vectormap.elem.poly;

import org.kabeja.dxf.DXFLWPolyline;

import com.mndk.kvm2m.core.projection.Grs80Projection;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vectormap.VMapElementType;
import com.mndk.kvm2m.core.vectormap.elem.IHasElevationData;
import com.mndk.ngiparser.ngi.element.NgiLineElement;

public class VMapContour extends VMapPolyline implements IHasElevationData {

	private final int elevation;

	public VMapContour(DXFLWPolyline polyline, Grs80Projection projection) {
		super(polyline, projection, VMapElementType.등고선);
		this.elevation = (int) polyline.getElevation();
	}

	public VMapContour(NgiLineElement polyline, Grs80Projection projection) {
		super(polyline, projection, VMapElementType.등고선);
		this.elevation = (int) Math.round((Double) polyline.getRowData("등고수치"));
	}

	public VMapContour(Vector2DH[] vertexes, int elevation) {
		super(vertexes, false);
		this.elevation = elevation;
	}
	
	@Override
	public int getElevation() {
		return this.elevation;
	}
}
