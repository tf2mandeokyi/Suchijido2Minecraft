package com.mndk.kmdi.core.dxfmap.elem.polyline;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kabeja.dxf.DXFLWPolyline;

import com.mndk.kmdi.core.dxfmap.DXFMapObjectType;
import com.mndk.kmdi.core.dxfmap.elem.IHasElevationData;
import com.mndk.kmdi.core.math.VectorMath;
import com.mndk.kmdi.core.projection.grs80.Grs80Projection;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;

public class DXFMapContour extends DXFMapPolyline implements IHasElevationData {

    private final int elevation;

    public DXFMapContour(DXFLWPolyline polyline, Grs80Projection projection) {
        super(polyline, projection, DXFMapObjectType.등고선);
        this.elevation = (int) polyline.getElevation();
    }

    public DXFMapContour(Vector2D[] vertexes, int elevation) {
    	super(vertexes);
    	this.elevation = elevation;
    }
    
    @Override
    public int getElevation() {
        return this.elevation;
    }

	/**
	 * @return The list of the entry, where the key is the intersection point and the value is the squared distance from the ray starting point.
	 * */
	public List<Map.Entry<Vector, Double>> getStraightLineIntersections(Vector2D sLineP0, Vector2D sLinePDelta) {
		
		List<Map.Entry<Vector, Double>> result = new ArrayList<>();
		
		for(int i=0;i<this.getVertexCount()-1;i++) {
			Vector2D intersectionPoint = VectorMath.getLineStraightIntersection(
					sLineP0, sLinePDelta, 
					this.getVertex(i), this.getVertex(i+1)
			);
			
			if(intersectionPoint != null) {
				result.add(new AbstractMap.SimpleEntry<>(
						intersectionPoint.toVector(this.getElevation()), 
						sLineP0.distanceSq(intersectionPoint)
				));
			}
		}
		return result;
	}
}
