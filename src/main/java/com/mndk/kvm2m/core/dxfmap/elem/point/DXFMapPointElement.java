package com.mndk.kvm2m.core.dxfmap.elem.point;

import org.kabeja.dxf.DXFPoint;

import com.mndk.kvm2m.core.dxfmap.DXFMapObjectType;
import com.mndk.kvm2m.core.dxfmap.elem.DXFMapElement;
import com.mndk.kvm2m.core.projection.grs80.Grs80Projection;
import com.mndk.kvm2m.core.util.math.Vector2DH;

public class DXFMapPointElement extends DXFMapElement<DXFPoint> {

	private final Vector2DH point;

    public DXFMapPointElement(DXFPoint point, Grs80Projection projection, DXFMapObjectType type) {
    	super(type);
        this.point = projectGrs80CoordToBteCoord(projection, point.getX(), point.getY());
    }
    
    public Vector2DH getPosition() {
    	return this.point;
    }
	
}
