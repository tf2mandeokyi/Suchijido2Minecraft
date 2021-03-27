package com.mndk.kvm2m.core.vectormap.elem.poly;

import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFVertex;

import com.mndk.kvm2m.core.projection.Grs80Projection;
import com.mndk.kvm2m.core.util.LineGenerator;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.math.VectorMath;
import com.mndk.kvm2m.core.util.shape.BoundingBox;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vectormap.VMapObjectType;
import com.mndk.kvm2m.core.vectormap.elem.VMapElement;
import com.mndk.ngiparser.ngi.element.NgiLineElement;
import com.mndk.ngiparser.ngi.element.NgiPolygonElement;
import com.mndk.ngiparser.ngi.vertex.NgiVertex;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.FlatRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VMapPolyline extends VMapElement {

	
    private Vector2DH[][] vertexList;
    private final boolean closed;

    
    private VMapPolyline(VMapObjectType type, boolean closed) {
    	super(type);
        this.closed = closed;
    }

    
    public VMapPolyline(DXFLWPolyline polyline, Grs80Projection projection, VMapObjectType type) {
    	this(type, polyline.isClosed());
        this.vertexList = new Vector2DH[1][polyline.getVertexCount()];
        
        for(int i = 0; i < polyline.getVertexCount(); ++i) {
            DXFVertex vertex = polyline.getVertex(i);
            this.vertexList[0][i] = projectGrs80CoordToBteCoord(projection, vertex.getX(), vertex.getY());
        }
        this.getBoundingBox();
    }

    
    public VMapPolyline(NgiPolygonElement polyline, Grs80Projection projection, VMapObjectType type) {
    	this(type, true);
        this.vertexList = new Vector2DH[polyline.vertexData.length][];
        
        for(int j = 0; j < polyline.vertexData.length; ++j) {
        	int size = polyline.vertexData[j].getSize();
            this.vertexList[j] = new Vector2DH[size];
            
            for(int i = 0; i < size; ++i) {
                NgiVertex vertex = polyline.vertexData[0].getVertex(i);
                this.vertexList[j][i] = projectGrs80CoordToBteCoord(projection, vertex.getAxis(0), vertex.getAxis(1));
            }
        }
        this.getBoundingBox();
    }

    
    public VMapPolyline(NgiLineElement line, Grs80Projection projection, VMapObjectType type) {
    	this(type, false);
    	int size = line.lineData.getSize();
        this.vertexList = new Vector2DH[1][size];
        
        for(int i = 0; i < size; ++i) {
            NgiVertex vertex = line.lineData.getVertex(i);
            this.vertexList[0][i] = projectGrs80CoordToBteCoord(projection, vertex.getAxis(0), vertex.getAxis(1));
        }
        this.getBoundingBox();
    }
    
    
    public VMapPolyline(Vector2DH[] vertexes, boolean closed) {
    	this(VMapObjectType.기타경계, closed);
    	this.vertexList = new Vector2DH[][] {vertexes};
        this.getBoundingBox();
    }
    
    
    public VMapPolyline(Polygonal2DRegion region) {
    	this(VMapObjectType.기타경계, true);
    	int n = region.size();
    	this.vertexList = new Vector2DH[1][n];
    	
    	for(int i = 0; i < n; i++) {
        	this.vertexList[0][i] = new Vector2DH(region.getPoints().get(i));
    	}
        this.getBoundingBox();
    }
    
    
    public boolean containsPoint(Vector2DH point) {
    	
    	for(int k = 0; k < vertexList.length; ++k) {
        	boolean inside = false;
        	Vector2DH[] temp = vertexList[k];
        	for(int i = 0, j = temp.length - 1; i < temp.length; j = i++) {
        		if(VectorMath.checkRayXIntersection(point, temp[i], temp[j])) {
                	inside = !inside;
        		}
        	}
        	if(inside) return true;
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
    	return boundingBoxResult = new BoundingBox(minX, minZ, maxX, maxZ);
    }
	
	
	
	public void generateBlocks(FlatRegion region, World w, TriangleList triangleList) {
		
		this.generateOutline(region, w, triangleList);
		
		if(this.isClosed()) {
			this.fillBlocks(region, w, triangleList);
		}
	}
	
	
	protected void generateOutline(FlatRegion region, World w, TriangleList triangleList) {
		int y = this.getType().getDefaultHeight();
		IBlockState state = this.getType().getBlockState();
		
		if(state == null) return;
		
		LineGenerator lineGenerator = new LineGenerator(
				v -> (int) Math.round(triangleList.interpolateHeight(v)) + y, 
				w, region, state
		);
		
		for(int j = 0; j < vertexList.length; ++j) {
			Vector2DH[] temp = vertexList[j];
        	for(int i = 0; i < temp.length - 1; ++i) {
        		lineGenerator.generateLine(temp[i], temp[i+1]);
            }
        	if(this.isClosed()) {
        		lineGenerator.generateLine(temp[temp.length-1], temp[0]);
        	}
		}
	}
	
	
	private void fillBlocks(FlatRegion region, World w, TriangleList triangleList) {
		
		IBlockState state = this.getType().getBlockState();
		
		if(state == null) return;

		Vector region_vmin = region.getMinimumPoint(), region_vmax = region.getMaximumPoint();
		int region_xmin = (int) Math.floor(region_vmin.getX()), region_xmax = (int) Math.ceil(region_vmax.getX());
		int region_zmin = (int) Math.floor(region_vmin.getZ()), region_zmax = (int) Math.ceil(region_vmax.getZ());
		
		BoundingBox box = this.getBoundingBox();
		int vertex_xmin = (int) Math.floor(box.xmin), vertex_xmax = (int) Math.ceil(box.xmax);
		int vertex_zmin = (int) Math.floor(box.zmin), vertex_zmax = (int) Math.ceil(box.zmax);
		
		int xmin = Math.max(region_xmin, vertex_xmin), zmin = Math.max(region_zmin, vertex_zmin);
		int xmax = Math.min(region_xmax, vertex_xmax), zmax = Math.min(region_zmax, vertex_zmax);
		
		for(int z = zmin; z <= zmax; ++z) {
			for(int x = xmin; x <= xmax; ++x) {
				if(!region.contains(new Vector(x, region.getMinimumY(), z)) || !this.containsPoint(new Vector2DH(x+.5, z+.5))) continue;
				int y = getHeightValueOfPoint(new Vector2DH(x, z), triangleList);
				w.setBlockState(new BlockPos(x, y, z), state);
			}
		}
	}
	
	
	protected int getHeightValueOfPoint(Vector2DH v, TriangleList triangleList) {
		return (int) Math.round(triangleList.interpolateHeight(v)) + this.getType().getDefaultHeight();
	}

    
    public Vector2DH[][] getVertexList() {
        return this.vertexList;
    }
    
    
    public boolean isClosed() {
    	return closed;
    }
    
}
