package com.mndk.kvm2m.core.vectormap.elem.poly;

import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFVertex;

import com.mndk.kvm2m.core.projection.grs80.Grs80Projection;
import com.mndk.kvm2m.core.util.LineGenerator;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.math.VectorMath;
import com.mndk.kvm2m.core.util.shape.BoundingBox;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vectormap.VectorMapObjectType;
import com.mndk.kvm2m.core.vectormap.elem.VectorMapElement;
import com.mndk.ngiparser.ngi.element.NgiLineElement;
import com.mndk.ngiparser.ngi.element.NgiPolygonElement;
import com.mndk.ngiparser.ngi.vertex.NgiVertex;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.FlatRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

public class VectorMapPolyline extends VectorMapElement {

	
    private final Vector2DH[][] vertexList;
    private final boolean closed;

    
    public VectorMapPolyline(DXFLWPolyline polyline, Grs80Projection projection, VectorMapObjectType type) {
    	super(type);
        this.vertexList = new Vector2DH[1][polyline.getVertexCount()];
        this.closed = polyline.isClosed();
        
        for(int i = 0; i < polyline.getVertexCount(); ++i) {
            DXFVertex vertex = polyline.getVertex(i);
            this.vertexList[0][i] = projectGrs80CoordToBteCoord(projection, vertex.getX(), vertex.getY());
        }
    }

    
    public VectorMapPolyline(NgiPolygonElement polyline, Grs80Projection projection, VectorMapObjectType type) {
    	super(type);
        this.closed = true;
        this.vertexList = new Vector2DH[polyline.vertexData.length][];
        
        for(int j = 0; j < polyline.vertexData.length; ++j) {
        	int size = polyline.vertexData[j].getSize();
            this.vertexList[j] = new Vector2DH[size];
            
            for(int i = 0; i < size; ++i) {
                NgiVertex vertex = polyline.vertexData[0].getVertex(i);
                this.vertexList[j][i] = projectGrs80CoordToBteCoord(projection, vertex.getAxis(0), vertex.getAxis(1));
            }
        }
    }

    
    public VectorMapPolyline(NgiLineElement line, Grs80Projection projection, VectorMapObjectType type) {
    	super(type);
        this.closed = false;
    	int size = line.lineData.getSize();
        this.vertexList = new Vector2DH[1][size];
        
        for(int i = 0; i < size; ++i) {
            NgiVertex vertex = line.lineData.getVertex(i);
            this.vertexList[0][i] = projectGrs80CoordToBteCoord(projection, vertex.getAxis(0), vertex.getAxis(1));
        }
    }
    
    
    public VectorMapPolyline(Vector2DH[] vertexes) {
    	super(null);
    	this.vertexList = new Vector2DH[][] {vertexes};
    	this.closed = false;
    }
    
    
    public VectorMapPolyline(Polygonal2DRegion region) {
    	super(null);
    	int n = region.size();
    	this.vertexList = new Vector2DH[1][n];
    	
    	for(int i = 0; i < n; i++) {
        	this.vertexList[0][i] = new Vector2DH(region.getPoints().get(i));
    	}
    	this.closed = false;
    }
    
    
    public boolean containsPoint(Vector2DH point) {
    	for(int j = 0; j < vertexList.length; ++j) {
        	int count = 0;
        	Vector2DH[] temp = vertexList[j];
        	for(int i = 0; i < temp.length - 1; ++i) {
        		if(VectorMath.getLineRayIntersection(point, Vector2DH.UNIT_X, temp[i], temp[i+1]) != null) {
        			count++;
        		}
        		if(this.isClosed() && VectorMath.getLineRayIntersection(point, Vector2DH.UNIT_X, temp[temp.length-1], temp[0]) != null) {
        			count++;
        		}
        	}
        	if (count % 2 == 1) return true;
    	}
    	return false;
    }
    
    
    private BoundingBox boundingBoxResult;
    
    public BoundingBox getBoundingBox() {
    	if(boundingBoxResult != null) return boundingBoxResult;
    	double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE, minZ = Double.MAX_VALUE, maxZ = Double.MIN_VALUE;
    	for(Vector2DH[] va : vertexList) for(Vector2DH v : va) {
    		if(v.x < minX) minX = v.x;
    		if(v.x > maxX) maxX = v.x;
    		if(v.z < minZ) minZ = v.z;
    		if(v.z > maxZ) maxZ = v.z;
    	}
    	return boundingBoxResult = new BoundingBox(minX, minZ, maxX-minX, maxZ-minZ);
    }
    
    
	public boolean checkLineIntersection(Vector2DH p0, Vector2DH p1) {
		Vector2DH dp = p1.sub2d(p0);
		for(int j = 0; j < vertexList.length; ++j) {
			for(int i = 0; i < vertexList[j].length - 1; ++i) {
	    		if(VectorMath.getLineStraightIntersection(p0, dp, vertexList[j][i], vertexList[j][i+1]) != null) {
	    			return true;
	    		}
	    	}
		}
		return false;
	}
	
	
	
	public void generateBlocks(FlatRegion region, World w, TriangleList triangleList) {
		
		int y = this.getType().getDefaultHeight();
		IBlockState state = this.getType().getBlockState();
		
		if(state == null) return;
		
		LineGenerator.world = w;
		LineGenerator.state = state;
		LineGenerator.region = region;
		LineGenerator.getYFunction = v -> {
			double height = triangleList.interpolateHeight(v);
			return (int) Math.round(height) + y;
		};
		
		for(int j = 0; j < vertexList.length; ++j) {
			Vector2DH[] temp = vertexList[j];
        	for(int i = 0; i < temp.length - 1; ++i) {
        		LineGenerator.generateLine(temp[i], temp[i+1]);
            }
        	if(this.isClosed()) {
                LineGenerator.generateLine(temp[temp.length-1], temp[0]);
        	}
		}
	}
	
	
	
	@Deprecated
	public void generateBlocks_old(FlatRegion region, World w, TriangleList triangleList) {
		
		int y = this.getType().getDefaultHeight();
		IBlockState state = this.getType().getBlockState();
		
		if(state == null) return;
		
		LineGenerator.world = w;
		LineGenerator.state = state;
		LineGenerator.region = region;
		LineGenerator.getYFunction = v -> {
			double height = triangleList.interpolateHeight(v);
			return (int) Math.round(height) + y;
		};
		
		if(region instanceof CuboidRegion) {
			BoundingBox box = new BoundingBox((CuboidRegion) region);
			
			for(int j = 0; j < vertexList.length; ++j) {
				Vector2DH[] temp = vertexList[j];
	        	for(int i = 0; i < temp.length - 1; ++i) {
	        		if(box.checkLineInside(temp[i], temp[i+1])) {
		                LineGenerator.generateLine(temp[i], temp[i+1]);
	        		}
	            }
	        	if(this.isClosed() && box.checkLineInside(temp[temp.length-1], temp[0])) {
	                LineGenerator.generateLine(temp[temp.length-1], temp[0]);
	        	}
			}
		}
		else if(region instanceof Polygonal2DRegion) {
        	VectorMapPolyline polySelection = new VectorMapPolyline((Polygonal2DRegion) region);

        	for(int j = 0; j < vertexList.length; ++j) {
				Vector2DH[] temp = vertexList[j];
	        	for(int i = 0; i < temp.length - 1; ++i) {
	        		if(polySelection.checkLineIntersection(temp[i], temp[i+1])) {
		                LineGenerator.generateLine(temp[i], temp[i+1]);
	        		}
	            }
	        	if(this.isClosed() && polySelection.checkLineIntersection(temp[temp.length-1], temp[0])) {
	                LineGenerator.generateLine(temp[temp.length-1], temp[0]);
	        	}
			}
		}
	}

    
    public Vector2DH[][] getVertexList() {
        return this.vertexList;
    }
    
    
    public boolean isClosed() {
    	return closed;
    }

}
