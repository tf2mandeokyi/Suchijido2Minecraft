package com.mndk.kvm2m.core.vectormap.elem.point;

import org.kabeja.dxf.DXFPoint;

import com.mndk.kvm2m.core.projection.grs80.Grs80Projection;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vectormap.VectorMapObjectType;
import com.mndk.kvm2m.core.vectormap.elem.IHasElevationData;
import com.mndk.ngiparser.ngi.element.NgiPointElement;

public class VectorMapElevationPoint extends VectorMapPoint implements IHasElevationData {

	private int elevation;
	
	public VectorMapElevationPoint(DXFPoint point, Grs80Projection projection) {
		super(point, projection, VectorMapObjectType.표고점);
		this.elevation = (int) Math.round(point.getZ());
	}
	
	public VectorMapElevationPoint(NgiPointElement point, Grs80Projection projection) {
		super(point, projection, VectorMapObjectType.표고점);
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
