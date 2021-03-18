package com.mndk.kvm2m.core.dxfmap.elem.polyline;

import org.kabeja.dxf.DXFLWPolyline;

import com.mndk.kvm2m.core.dxfmap.DXFMapObjectType;
import com.mndk.kvm2m.core.dxfmap.elem.IHasElevationData;
import com.mndk.kvm2m.core.projection.grs80.Grs80Projection;
import com.mndk.kvm2m.core.util.math.Vector2DH;

public class DXFMapContour extends DXFMapPolyline implements IHasElevationData {

    private final int elevation;

    public DXFMapContour(DXFLWPolyline polyline, Grs80Projection projection) {
        super(polyline, projection, DXFMapObjectType.등고선);
        this.elevation = (int) polyline.getElevation();
    }

    public DXFMapContour(Vector2DH[] vertexes, int elevation) {
    	super(vertexes);
    	this.elevation = elevation;
    }
    
    @Override
    public int getElevation() {
        return this.elevation;
    }
}
