package com.mndk.kmdi.core.dxfmap.elem.point;

import org.kabeja.dxf.DXFPoint;

import com.mndk.kmdi.core.dxfmap.DXFMapObjectType;
import com.mndk.kmdi.core.dxfmap.elem.IHasElevationData;
import com.mndk.kmdi.core.projection.grs80.Grs80Projection;

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

}
