package com.mndk.kvm2m.core.vectormap.elem.point;

import org.kabeja.dxf.DXFPoint;

import com.mndk.kvm2m.core.projection.grs80.Grs80Projection;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vectormap.VectorMapObjectType;
import com.mndk.kvm2m.core.vectormap.elem.VectorMapElement;
import com.mndk.ngiparser.ngi.element.NgiPointElement;

public class VectorMapPoint extends VectorMapElement {

	private final Vector2DH point;

    public VectorMapPoint(DXFPoint point, Grs80Projection projection, VectorMapObjectType type) {
    	super(type);
        this.point = projectGrs80CoordToBteCoord(projection, point.getX(), point.getY());
    }

    public VectorMapPoint(NgiPointElement point, Grs80Projection projection, VectorMapObjectType type) {
    	super(type);
        this.point = projectGrs80CoordToBteCoord(projection, point.position.getAxis(0), point.position.getAxis(1));
    }
    
    public Vector2DH getPosition() {
    	return this.point;
    }
	
}
