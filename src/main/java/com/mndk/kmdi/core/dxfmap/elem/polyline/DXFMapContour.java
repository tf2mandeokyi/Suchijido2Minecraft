package com.mndk.kmdi.core.dxfmap.elem.polyline;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kabeja.dxf.DXFLWPolyline;

import com.mndk.kmdi.core.dxfmap.elem.interf.IHasElevationData;
import com.mndk.kmdi.core.math.VectorMath;
import com.mndk.kmdi.core.projection.grs80.Grs80Projection;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;

public class DXFMapContour extends DXFMapPolyline implements IHasElevationData {

    private final int elevation;

    public DXFMapContour(DXFLWPolyline polyline, Grs80Projection projection) {
        super(polyline, projection);
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
	public List<Map.Entry<Vector, Double>> getRayIntersections(Vector2D rayStart, Vector2D rayDelta) {
		
		List<Map.Entry<Vector, Double>> result = new ArrayList<>();
		
		for(int i=0;i<this.getVertexCount()-1;i++) {
			Vector2D intersectionPoint = VectorMath.getLineRayIntersection(
					rayStart, rayDelta, 
					this.getVertex(i), this.getVertex(i+1)
			);
			
			if(intersectionPoint != null) {
				result.add(new AbstractMap.SimpleEntry<>(
						intersectionPoint.toVector(this.getElevation()), 
						rayStart.distanceSq(intersectionPoint)
				));
			}
		}
		return result;
	}

	public Map.Entry<Vector2D, Double> getClosestPointToPoint(Vector2D p) {
		
		double shortestLength = Double.POSITIVE_INFINITY, tempLength;
		Vector2D closestPoint = null, tempPoint;
		
		for(int i=0;i<this.getVertexCount()-1;i++) {
			tempPoint = VectorMath.getClosestPointToLine(p, this.getVertex(i), this.getVertex(i+1));
			tempLength = p.distanceSq(tempPoint);
			if(shortestLength > tempLength) {
				closestPoint = tempPoint;
				shortestLength = tempLength;
			}
		}
		return new AbstractMap.SimpleEntry<>(closestPoint, shortestLength);
	}
}
