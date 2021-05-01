package com.mndk.kvm2m.core.vectormap.elem;

import java.util.Map;

import com.mndk.kvm2m.core.util.LineGenerator;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.math.VectorMath;
import com.mndk.kvm2m.core.util.shape.IntegerBoundingBox;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vectormap.VMapElementStyleSelector;
import com.mndk.kvm2m.core.vectormap.VMapElementStyleSelector.VMapElementStyle;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VMapPolyline extends VMapElement {

	
	private Vector2DH[][] vertexList;
	private final boolean doFill;

	
	private VMapPolyline(VMapElementLayer parent, Map<String, Object> dataRow, boolean doFill) {
		super(parent, dataRow);
		this.doFill = doFill;
	}

	
	private VMapPolyline(VMapElementLayer parent, Object[] dataRow, boolean doFill) {
		super(parent, dataRow);
		this.doFill = doFill;
	}
	
	
	public VMapPolyline(VMapElementLayer parent, Vector2DH[] vertexes, Map<String, Object> dataRow, boolean doFill) {
		this(parent, dataRow, doFill);
		this.vertexList = new Vector2DH[][] {vertexes};
		this.getBoundingBox();
	}
	
	
	public VMapPolyline(VMapElementLayer parent, Vector2DH[] vertexes, Object[] dataRow, boolean doFill) {
		this(parent, dataRow, doFill);
		this.vertexList = new Vector2DH[][] {vertexes};
		this.getBoundingBox();
	}
	
	
	public VMapPolyline(VMapElementLayer parent, Vector2DH[][] vertexes, Map<String, Object> dataRow, boolean doFill) {
		this(parent, dataRow, doFill);
		this.vertexList = vertexes;
		this.getBoundingBox();
	}
	
	
	public VMapPolyline(VMapElementLayer parent, Vector2DH[][] vertexes, Object[] dataRow, boolean doFill) {
		this(parent, dataRow, doFill);
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
		Vector2DH[][] newVertexList = new Vector2DH[this.vertexList.length + other.vertexList.length][];
		System.arraycopy(this.vertexList, 0, newVertexList, 0, this.vertexList.length);
		System.arraycopy(other.vertexList, 0, newVertexList, this.vertexList.length, other.vertexList.length);
		
		return new VMapPolyline(this.parent, newVertexList, this.dataRow, this.doFill);
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
			
		//if(!this.isClosed() || this.parent.getType() == VMapElementType.건물) { // TODO
			this.generateOutline(region, w, triangleList);
		//}
		
		if(this.shouldBeFilled()) {
			this.fillBlocks(region, w, triangleList, box);
		}
	}
	
	
	protected void generateOutline(FlatRegion region, World w, TriangleList triangleList) {
		
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
				if(this.shouldBeFilled()) {
					lineGenerator.generateLine(temp[temp.length-1], temp[0]);
				}
			}
		}
	}
	
	
	protected void fillBlocks(FlatRegion region, World w, TriangleList triangleList, IntegerBoundingBox limitBox) {
		
		VMapElementStyle[] styles = VMapElementStyleSelector.getStyle(this);
		if(styles == null) return;
		for(VMapElementStyle style : styles) {
			if(style == null) continue; if(style.state == null) continue;
			
			for(int z = limitBox.zmin; z <= limitBox.zmax; ++z) {
				for(int x = limitBox.xmin; x <= limitBox.xmax; ++x) {
					if(!region.contains(new Vector(x, region.getMinimumY(), z)) || !this.containsPoint(new Vector2DH(x+.5, z+.5))) continue;
					int y = (int) Math.round(triangleList.interpolateHeight(x, z)) + style.y;
					w.setBlockState(new BlockPos(x, y, z), style.state);
				}
			}
		}
	}

	
	public Vector2DH[][] getVertexList() {
		return this.vertexList;
	}
	
	
	public boolean shouldBeFilled() {
		return doFill;
	}
	
}
