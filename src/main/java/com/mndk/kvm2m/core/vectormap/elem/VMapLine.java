package com.mndk.kvm2m.core.vectormap.elem;

import java.util.Map;

import com.mndk.kvm2m.core.util.LineGenerator;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.shape.IntegerBoundingBox;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vectormap.VMapElementStyleSelector;
import com.mndk.kvm2m.core.vectormap.VMapElementStyleSelector.VMapElementStyle;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.world.World;

public class VMapLine extends VMapElement {

	
	private Vector2DH[][] vertexList;
	private boolean isClosed;

	
	private VMapLine(VMapElementLayer parent, Map<String, Object> dataRow, boolean isClosed) {
		super(parent, dataRow);
		this.isClosed = isClosed;
	}

	
	private VMapLine(VMapElementLayer parent, Object[] dataRow, boolean isClosed) {
		super(parent, dataRow);
		this.isClosed = isClosed;
	}
	
	
	public VMapLine(VMapElementLayer parent, Vector2DH[] vertexes, Map<String, Object> dataRow, boolean isClosed) {
		this(parent, dataRow, isClosed);
		this.vertexList = new Vector2DH[][] {vertexes};
		this.getBoundingBox();
	}
	
	
	public VMapLine(VMapElementLayer parent, Vector2DH[] vertexes, Object[] dataRow, boolean isClosed) {
		this(parent, dataRow, isClosed);
		this.vertexList = new Vector2DH[][] {vertexes};
		this.getBoundingBox();
	}
	
	
	public VMapLine(VMapElementLayer parent, Vector2DH[][] vertexes, Map<String, Object> dataRow, boolean isClosed) {
		this(parent, dataRow, isClosed);
		this.vertexList = vertexes;
		this.getBoundingBox();
	}
	
	
	public VMapLine(VMapElementLayer parent, Vector2DH[][] vertexes, Object[] dataRow, boolean isClosed) {
		this(parent, dataRow, isClosed);
		this.vertexList = vertexes;
		this.getBoundingBox();
	}
	
	
	public VMapPolyline merge(VMapLine other) {
		Vector2DH[][] newVertexList = new Vector2DH[this.vertexList.length + other.vertexList.length][];
		System.arraycopy(this.vertexList, 0, newVertexList, 0, this.vertexList.length);
		System.arraycopy(other.vertexList, 0, newVertexList, this.vertexList.length, other.vertexList.length);
		
		return new VMapPolyline(this.parent, newVertexList, this.dataRow, this.isClosed);
	}
	
	
	private IntegerBoundingBox boundingBoxResult;
	public IntegerBoundingBox getBoundingBox() {
		if(boundingBoxResult != null) return boundingBoxResult;
		int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, 
			maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
		for(Vector2DH[] va : vertexList) for(Vector2DH v : va) {
			if(v.x < minX) minX = (int) Math.floor(v.x);
			if(v.x > maxX) maxX = (int) Math.ceil(v.x);
			if(v.z < minZ) minZ = (int) Math.floor(v.z);
			if(v.z > maxZ) maxZ = (int) Math.ceil(v.z);
		}
		return boundingBoxResult = new IntegerBoundingBox(minX, minZ, maxX, maxZ);
	}
	
	
	@Override
	public final void generateBlocks(FlatRegion region, World w, TriangleList triangleList) {
		
		IntegerBoundingBox box = this.getBoundingBox().getIntersectionArea(new IntegerBoundingBox(region));
		if(!box.isValid()) return;
			
		VMapElementStyle[] styles = VMapElementStyleSelector.getStyle(this);
		if(styles == null) return;
		for(VMapElementStyle style : styles) {
			if(style == null) continue; if(style.state == null) continue;
			
			LineGenerator lineGenerator = new LineGenerator(
					(x, z) -> (int) Math.round(triangleList.interpolateHeight(x, z)) + style.y, 
					w, region, style.state
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
	}

	
	public Vector2DH[][] getVertexList() {
		return this.vertexList;
	}
	
	
	public boolean isClosed() {
		return this.isClosed;
	}
}