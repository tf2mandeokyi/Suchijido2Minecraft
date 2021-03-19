package com.mndk.kvm2m.core.vectormap.elem.poly;

import org.kabeja.dxf.DXFLWPolyline;

import com.mndk.kvm2m.core.projection.grs80.Grs80Projection;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vectormap.VectorMapObjectType;
import com.mndk.kvm2m.core.vectormap.elem.IHasElevationData;

public class VectorMapContour extends VectorMapPolyline implements IHasElevationData {

    private final int elevation;

    public VectorMapContour(DXFLWPolyline polyline, Grs80Projection projection) {
        super(polyline, projection, VectorMapObjectType.등고선);
        this.elevation = (int) polyline.getElevation();
    }

    public VectorMapContour(Vector2DH[] vertexes, int elevation) {
    	super(vertexes);
    	this.elevation = elevation;
    }
    
    @Override
    public int getElevation() {
        return this.elevation;
    }
}
