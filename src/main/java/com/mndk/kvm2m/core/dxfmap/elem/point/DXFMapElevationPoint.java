package com.mndk.kvm2m.core.dxfmap.elem.point;

import org.kabeja.dxf.DXFPoint;

import com.mndk.kvm2m.core.dxfmap.DXFMapObjectType;
import com.mndk.kvm2m.core.dxfmap.elem.IHasElevationData;
import com.mndk.kvm2m.core.projection.grs80.Grs80Projection;
import com.mndk.kvm2m.core.util.math.Vector2DH;

public class DXFMapElevationPoint extends DXFMapPointElement implements IHasElevationData {

	private int elevation;
	
	public DXFMapElevationPoint(DXFPoint point, Grs80Projection projection) {
		super(point, projection, DXFMapObjectType.표고점);
		this.elevation = (int) point.getZ();
	}

	@Override
	public int getElevation() {
		return this.elevation;
	}
	
	public Vector2DH toVector() {
		return this.getPosition().withHeight(elevation);
	}

}
