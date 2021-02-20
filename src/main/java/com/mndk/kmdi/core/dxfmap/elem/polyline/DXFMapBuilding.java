package com.mndk.kmdi.core.dxfmap.elem.polyline;

import org.kabeja.dxf.DXFLWPolyline;

import com.mndk.kmdi.core.projection.grs80.Grs80Projection;

@Deprecated
public class DXFMapBuilding extends DXFMapPolyline {

	public DXFMapBuilding(DXFLWPolyline polyline, Grs80Projection projection) {
		super(polyline, projection);
	}

}
