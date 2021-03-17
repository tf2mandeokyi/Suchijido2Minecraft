package com.mndk.kmdi.core.dxfmap.elem.polyline;

import java.util.AbstractMap;
import java.util.Map;

import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFVertex;

import com.mndk.kmdi.core.dxfmap.DXFMapObjectType;
import com.mndk.kmdi.core.dxfmap.elem.DXFMapElement;
import com.mndk.kmdi.core.projection.grs80.Grs80Projection;
import com.mndk.kmdi.core.util.LineGenerator;
import com.mndk.kmdi.core.util.math.VectorMath;
import com.mndk.kmdi.core.util.shape.BoundingBox;
import com.mndk.kmdi.core.util.shape.TriangleList;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

public class DXFMapPolyline extends DXFMapElement<DXFLWPolyline> {

	
    private final Vector2D[] vertexList;
    private final boolean closed;

    
    public DXFMapPolyline(DXFLWPolyline polyline, Grs80Projection projection, DXFMapObjectType type) {
    	super(type);
        this.vertexList = new Vector2D[polyline.getVertexCount()];
        this.closed = polyline.isClosed();
        for(int i=0;i<polyline.getVertexCount();i++) {
            DXFVertex vertex = polyline.getVertex(i);
            this.vertexList[i] = projectGrs80CoordToBteCoord(projection, vertex.getX(), vertex.getY());
        }
    }
    
    
    public DXFMapPolyline(Vector2D[] vertexes) {
    	super(null);
    	this.vertexList = vertexes;
    	this.closed = false;
    }
    
    
    public DXFMapPolyline(Polygonal2DRegion region) {
    	super(null);
    	this.vertexList = region.getPoints().toArray(new Vector2D[0]);
    	this.closed = false;
    } 
    
    
    public boolean containsPoint(Vector2D point) {
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
    		if(VectorMath.getLineStraightIntersection(p0, dp, vertexList[i], vertexList[i+1]) != null) {
    			return true;
    		}
    	}
		return false;
	}
	

	
	public void generateFlatPolygon(Region region, World w, int y, IBlockState state) {

		LineGenerator.region = region;
		LineGenerator.world = w;
		LineGenerator.state = state;
		LineGenerator.y = y;
		
		if(region instanceof CuboidRegion) {
			BoundingBox box = new BoundingBox((CuboidRegion) region);
        	for(int i=0;i<this.getVertexCount()-1;i++) {
        		if(box.checkLineInside(this.getVertex(i), this.getVertex(i+1))) {
	                LineGenerator.generateFlatLine(getVertex(i), getVertex(i+1));
        		}
            }
		}
		else if(region instanceof Polygonal2DRegion) {
        	DXFMapPolyline polySelection = new DXFMapPolyline((Polygonal2DRegion) region);
        	
        	for(int i=0;i<this.getVertexCount()-1;i++) {
        		if(polySelection.checkLineIntersection(this.getVertex(i), this.getVertex(i+1))) {
	                LineGenerator.generateFlatLine(getVertex(i), getVertex(i+1));
        		}
            }
		}
	}
	
	
	
	public void generatePolygonOnTerrain(Region region, World w, IBlockState state, TriangleList triangleList) {
		
		LineGenerator.region = region;
		LineGenerator.world = w;
		LineGenerator.state = state;
		LineGenerator.getYFunction = v -> {
			double height = triangleList.interpolateY(v);
			return (int) (height == height ? Math.round(height) : 0);
		};
		
		if(region instanceof CuboidRegion) {
			BoundingBox box = new BoundingBox((CuboidRegion) region);
			
        	for(int i=0;i<this.getVertexCount()-1;i++) {
        		if(box.checkLineInside(this.getVertex(i), this.getVertex(i+1))) {
	                LineGenerator.generateLineByFunction(getVertex(i), getVertex(i+1));
        		}
            }
        	if(this.isClosed()) {
        		if(box.checkLineInside(this.getVertex(this.getVertexCount()-1), this.getVertex(0))) {
	                LineGenerator.generateLineByFunction(getVertex(this.getVertexCount()-1), getVertex(0));
        		}
        	}
		}
		else if(region instanceof Polygonal2DRegion) {
        	DXFMapPolyline polySelection = new DXFMapPolyline((Polygonal2DRegion) region);
        	
        	for(int i=0;i<this.getVertexCount()-1;i++) {
        		if(polySelection.checkLineIntersection(this.getVertex(i), this.getVertex(i+1))) {
	                LineGenerator.generateLineByFunction(getVertex(i), getVertex(i+1));
        		}
            }
        	if(this.isClosed()) {
        		if(polySelection.checkLineIntersection(this.getVertex(this.getVertexCount()-1), this.getVertex(0))) {
	                LineGenerator.generateLineByFunction(getVertex(this.getVertexCount()-1), getVertex(0));
        		}
        	}
		}
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
	

    public int getVertexCount() {
        return this.vertexList.length;
    }

    
    public Vector2D[] getVertexList() {
        return this.vertexList;
    }

    
    public Vector2D getVertex(int i) {
        return this.vertexList[i];
    }
    
    
    public boolean isClosed() {
    	return closed;
    }

}
