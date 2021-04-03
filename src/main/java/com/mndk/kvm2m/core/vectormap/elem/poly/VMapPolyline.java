package com.mndk.kvm2m.core.vectormap.elem.poly;

import java.util.Map;

import com.mndk.kvm2m.core.util.LineGenerator;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.math.VectorMath;
import com.mndk.kvm2m.core.util.shape.BoundingBox;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vectormap.VMapBlockSelector;
import com.mndk.kvm2m.core.vectormap.VMapElementType;
import com.mndk.kvm2m.core.vectormap.elem.VMapElement;
import com.mndk.kvm2m.core.vectormap.elem.VMapElementLayer;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VMapPolyline extends VMapElement {

	
	private Vector2DH[][] vertexList;
	private final boolean closed;

	
	private VMapPolyline(VMapElementLayer parent, Map<String, Object> dataRow, boolean closed) {
		super(parent, dataRow);
		this.closed = closed;
	}

	
	private VMapPolyline(VMapElementLayer parent, Object[] dataRow, boolean closed) {
		super(parent, dataRow);
		this.closed = closed;
	}
	
	
	public VMapPolyline(VMapElementLayer parent, Vector2DH[] vertexes, Map<String, Object> dataRow, boolean closed) {
		this(parent, dataRow, closed);
		this.vertexList = new Vector2DH[][] {vertexes};
		this.getBoundingBox();
	}
	
	
	public VMapPolyline(VMapElementLayer parent, Vector2DH[] vertexes, Object[] dataRow, boolean closed) {
		this(parent, dataRow, closed);
		this.vertexList = new Vector2DH[][] {vertexes};
		this.getBoundingBox();
	}
	
	
	public VMapPolyline(VMapElementLayer parent, Vector2DH[][] vertexes, Map<String, Object> dataRow, boolean closed) {
		this(parent, dataRow, closed);
		this.vertexList = vertexes;
		this.getBoundingBox();
	}
	
	
	public VMapPolyline(VMapElementLayer parent, Vector2DH[][] vertexes, Object[] dataRow, boolean closed) {
		this(parent, dataRow, closed);
		this.vertexList = vertexes;
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
	
	
	public VMapPolyline merge(VMapPolyline other) {
		/*
		if(this.getType() != other.getType()) {
			throw new RuntimeException(new VMapParserException("Cannot merge two different types of polygon together!"));
		}
		*/
		
		Vector2DH[][] newVertexList = new Vector2DH[this.vertexList.length + other.vertexList.length][];
		System.arraycopy(this.vertexList, 0, newVertexList, 0, this.vertexList.length);
		System.arraycopy(other.vertexList, 0, newVertexList, this.vertexList.length, other.vertexList.length);
		
		return new VMapPolyline(this.parent, newVertexList, this.dataRow, this.closed);
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
	
	
	@Override
	public final void generateBlocks(FlatRegion region, World w, TriangleList triangleList) {
		
		if(!this.isClosed() || this.parent.getType() == VMapElementType.건물) { // TODO
			this.generateOutline(region, w, triangleList);
		}
		
		if(this.isClosed()) {
			this.fillBlocks(region, w, triangleList);
		}
	}
	
	
	protected void generateOutline(FlatRegion region, World w, TriangleList triangleList) {
		
		IBlockState state = VMapBlockSelector.getBlockState(this);
		if(state == null) return;
		
		int y = VMapBlockSelector.getAdditionalHeight(this);
		
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
	
	
	protected void fillBlocks(FlatRegion region, World w, TriangleList triangleList) {
		
		IBlockState state = VMapBlockSelector.getBlockState(this);
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
		return (int) Math.round(triangleList.interpolateHeight(v)) + VMapBlockSelector.getAdditionalHeight(this);
	}

	
	public Vector2DH[][] getVertexList() {
		return this.vertexList;
	}
	
	
	public boolean isClosed() {
		return closed;
	}
	
}
