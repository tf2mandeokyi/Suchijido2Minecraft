package com.mndk.kmdi.core.dxfmap.elem.polyline;

import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFVertex;

import com.mndk.kmdi.core.dxfmap.elem.DXFMapElement;
import com.mndk.kmdi.core.math.VectorMath;
import com.mndk.kmdi.core.math.shape.BoundingBox;
import com.mndk.kmdi.core.projection.grs80.Grs80Projection;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.Polygonal2DRegion;

public class DXFMapPolyline extends DXFMapElement<DXFLWPolyline> {

    private final Vector2D[] vertexList;
    private final boolean closed;

    public DXFMapPolyline(DXFLWPolyline polyline, Grs80Projection projection) {
        this.vertexList = new Vector2D[polyline.getVertexCount()];
        this.closed = polyline.isClosed();
        for(int i=0;i<polyline.getVertexCount();i++) {
            DXFVertex vertex = polyline.getVertex(i);
            this.vertexList[i] = projectGrs80CoordToBteCoord(projection, vertex.getX(), vertex.getY());
        }
    }
    
    public DXFMapPolyline(Vector2D[] vertexes) {
    	this.vertexList = vertexes;
    	this.closed = false;
    }
    
    public DXFMapPolyline(Polygonal2DRegion region) {
    	this.vertexList = region.getPoints().toArray(new Vector2D[0]);
    	this.closed = false;
    }
    
    public boolean containsPoint(Vector2D point) {
    	if(!this.closed) return false;
    	int count = 0;
    	for(int i=0;i<vertexList.length-1;i++) {
    		if(VectorMath.getLineRayIntersection(point, Vector2D.UNIT_X, vertexList[i], vertexList[i+1]) != null) {
    			count++;
    		}
    	}
    	return count % 2 == 1;
    }
    
    private BoundingBox boundingBoxResult;
    public BoundingBox getBoundingBox() {
    	if(boundingBoxResult != null) return boundingBoxResult;
    	double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE, minZ = Double.MAX_VALUE, maxZ = Double.MIN_VALUE;
    	for(Vector2D v : vertexList) {
    		if(v.getX() < minX) minX = v.getX();
    		if(v.getX() > maxX) maxX = v.getX();
    		if(v.getZ() < minZ) minZ = v.getZ();
    		if(v.getZ() > maxZ) maxZ = v.getZ();
    	}
    	return boundingBoxResult = new BoundingBox(minX, minZ, maxX-minX, maxZ-minZ);
    }
    
	public boolean checkLineIntersection(Vector2D p0, Vector2D p1) {
		Vector2D dp = p1.subtract(p0);
		for(int i=0;i<vertexList.length-1;i++) {
    		if(VectorMath.getLineRayIntersection(p0, dp, vertexList[i], vertexList[i+1].subtract(vertexList[i])) != null) {
    			return true;
    		}
    	}
		return false;
	}

    public int getVertexCount() {
        return this.vertexList.length;
    }

    public Vector2D[] getVertexList() {
        return this.vertexList;
    }

    public Vector2D getVertex(int i) {
        return this.vertexList[i];
    }

}
