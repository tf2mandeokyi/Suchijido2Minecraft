package com.mndk.kvm2m.core.dxfmap.elem.polyline;

import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFVertex;

import com.mndk.kvm2m.core.dxfmap.DXFMapObjectType;
import com.mndk.kvm2m.core.dxfmap.elem.DXFMapElement;
import com.mndk.kvm2m.core.projection.grs80.Grs80Projection;
import com.mndk.kvm2m.core.util.LineGenerator;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.math.VectorMath;
import com.mndk.kvm2m.core.util.shape.BoundingBox;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.FlatRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

public class DXFMapPolyline extends DXFMapElement<DXFLWPolyline> {

	
    private final Vector2DH[] vertexList;
    private final boolean closed;

    
    public DXFMapPolyline(DXFLWPolyline polyline, Grs80Projection projection, DXFMapObjectType type) {
    	super(type);
        this.vertexList = new Vector2DH[polyline.getVertexCount()];
        this.closed = polyline.isClosed();
        for(int i=0;i<polyline.getVertexCount();i++) {
            DXFVertex vertex = polyline.getVertex(i);
            this.vertexList[i] = projectGrs80CoordToBteCoord(projection, vertex.getX(), vertex.getY());
        }
    }
    
    
    public DXFMapPolyline(Vector2DH[] vertexes) {
    	super(null);
    	this.vertexList = vertexes;
    	this.closed = false;
    }
    
    
    public DXFMapPolyline(Polygonal2DRegion region) {
    	super(null);
    	int n = region.size();
    	this.vertexList = new Vector2DH[n];
    	for(int i = 0; i < n; i++) {
        	this.vertexList[i] = new Vector2DH(region.getPoints().get(i));
    	}
    	this.closed = false;
    } 
    
    
    public boolean containsPoint(Vector2DH point) {
    	int count = 0;
    	for(int i=0;i<vertexList.length-1;i++) {
    		if(VectorMath.getLineRayIntersection(point, Vector2DH.UNIT_X, vertexList[i], vertexList[i+1]) != null) {
    			count++;
    		}
    	}
    	return count % 2 == 1;
    }
    
    
    private BoundingBox boundingBoxResult;
    
    public BoundingBox getBoundingBox() {
    	if(boundingBoxResult != null) return boundingBoxResult;
    	double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE, minZ = Double.MAX_VALUE, maxZ = Double.MIN_VALUE;
    	for(Vector2DH v : vertexList) {
    		if(v.x < minX) minX = v.x;
    		if(v.x > maxX) maxX = v.x;
    		if(v.z < minZ) minZ = v.z;
    		if(v.z > maxZ) maxZ = v.z;
    	}
    	return boundingBoxResult = new BoundingBox(minX, minZ, maxX-minX, maxZ-minZ);
    }
    
    
	public boolean checkLineIntersection(Vector2DH p0, Vector2DH p1) {
		Vector2DH dp = p1.sub2d(p0);
		for(int i=0;i<vertexList.length-1;i++) {
    		if(VectorMath.getLineStraightIntersection(p0, dp, vertexList[i], vertexList[i+1]) != null) {
    			return true;
    		}
    	}
		return false;
	}
	
	
	
	public void generatePolygonOnTerrain(FlatRegion region, World w, IBlockState state, TriangleList triangleList) {
		
		LineGenerator.region = region;
		LineGenerator.world = w;
		LineGenerator.state = state;
		LineGenerator.getYFunction = v -> {
			double height = triangleList.interpolateHeight(v);
			return (int) Math.round(height);
		};
		
		LineGenerator.containsFunction = v -> {
			double height = triangleList.interpolateHeight(v);
			return (int) Math.round(height);
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
	

    public int getVertexCount() {
        return this.vertexList.length;
    }

    
    public Vector2DH[] getVertexList() {
        return this.vertexList;
    }

    
    public Vector2DH getVertex(int i) {
        return this.vertexList[i];
    }
    
    
    public boolean isClosed() {
    	return closed;
    }

}
