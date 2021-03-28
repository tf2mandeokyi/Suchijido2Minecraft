package com.mndk.kvm2m.core.vectormap.elem.point;

import org.kabeja.dxf.DXFPoint;

import com.mndk.kvm2m.core.projection.Grs80Projection;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vectormap.VMapElementType;
import com.mndk.kvm2m.core.vectormap.elem.IHasElevationData;
import com.mndk.ngiparser.ngi.element.NgiPointElement;

public class VMapElevationPoint extends VMapPoint implements IHasElevationData {

	private int elevation;
	
	public VMapElevationPoint(DXFPoint point, Grs80Projection projection) {
		super(point, projection, VMapElementType.표고점);
		this.elevation = (int) Math.round(point.getZ());
	}
	
	public VMapElevationPoint(NgiPointElement point, Grs80Projection projection) {
		super(point, projection, VMapElementType.표고점);
		this.elevation = (int) Math.round((Double) point.getRowData("수치"));
	}

	@Override
	public int getElevation() {
		return this.elevation;
	}
	
	public Vector2DH toVector() {
		return this.getPosition().withHeight(elevation);
	}

}
