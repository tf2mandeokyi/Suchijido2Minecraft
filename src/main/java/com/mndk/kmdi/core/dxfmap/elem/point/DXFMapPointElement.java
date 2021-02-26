package com.mndk.kmdi.core.dxfmap.elem.point;

import org.kabeja.dxf.DXFPoint;

import com.mndk.kmdi.core.dxfmap.DXFMapObjectType;
import com.mndk.kmdi.core.dxfmap.elem.DXFMapElement;
import com.mndk.kmdi.core.projection.grs80.Grs80Projection;
import com.sk89q.worldedit.Vector2D;

public class DXFMapPointElement extends DXFMapElement<DXFPoint> {

	private final Vector2D point;

    public DXFMapPointElement(DXFPoint point, Grs80Projection projection, DXFMapObjectType type) {
    	super(type);
        this.point = projectGrs80CoordToBteCoord(projection, point.getX(), point.getY());
    }
    
    public Vector2D getVector2D() {
    	return this.point;
    }
	
}
