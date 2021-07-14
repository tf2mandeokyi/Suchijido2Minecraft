package com.mndk.kvm2m.core.vmap.elem.line;

import com.mndk.kvm2m.core.util.LineGenerator;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.shape.IntegerBoundingBox;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vmap.VMapElementGeomType;
import com.mndk.kvm2m.core.vmap.VMapElementStyleSelector;
import com.mndk.kvm2m.core.vmap.VMapElementStyleSelector.VMapElementStyle;
import com.mndk.kvm2m.core.vmap.elem.VMapElement;
import com.mndk.kvm2m.core.vmap.elem.VMapLayer;
import com.mndk.kvm2m.core.vmap.elem.poly.VMapPolygon;
import com.sk89q.worldedit.regions.FlatRegion;
import net.minecraft.world.World;

import java.util.Map;

public class VMapLineString extends VMapElement {

	
	protected Vector2DH[][] vertices;
	private boolean isClosed;

	
	protected VMapLineString(VMapLayer parent, Map<String, Object> dataRow, boolean isClosed) {
		super(parent, dataRow, VMapElementGeomType.LINESTRING);
		this.isClosed = isClosed;
	}

	
	protected VMapLineString(VMapLayer parent, Object[] dataRow, boolean isClosed) {
		super(parent, dataRow, VMapElementGeomType.LINESTRING);
		this.isClosed = isClosed;
	}
	
	
	public VMapLineString(VMapLayer parent, Vector2DH[][] vertices, Map<String, Object> dataRow, boolean isClosed) {
		this(parent, dataRow, isClosed);
		this.vertices = vertices;
		this.getBoundingBox();
	}
	
	
	public VMapLineString(VMapLayer parent, Vector2DH[][] vertices, Object[] dataRow, boolean isClosed) {
		this(parent, dataRow, isClosed);
		this.vertices = vertices;
		this.getBoundingBox();
	}
	
	
	public VMapPolygon merge(VMapLineString other) {
		Vector2DH[][] newVertexList = new Vector2DH[this.vertices.length + other.vertices.length][];
		System.arraycopy(this.vertices, 0, newVertexList, 0, this.vertices.length);
		System.arraycopy(other.vertices, 0, newVertexList, this.vertices.length, other.vertices.length);
		
		return new VMapPolygon(this.parent, newVertexList, this.dataRow, this.isClosed);
	}
	
	
	private IntegerBoundingBox boundingBoxResult;
	public IntegerBoundingBox getBoundingBox() {
		if(boundingBoxResult != null) return boundingBoxResult;
		int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, 
			maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
		for(Vector2DH[] va : vertices) for(Vector2DH v : va) {
			if(v.x < minX) minX = (int) Math.floor(v.x);
			if(v.x > maxX) maxX = (int) Math.ceil(v.x);
			if(v.z < minZ) minZ = (int) Math.floor(v.z);
			if(v.z > maxZ) maxZ = (int) Math.ceil(v.z);
		}
		return boundingBoxResult = new IntegerBoundingBox(minX, minZ, maxX, maxZ);
	}
	
	
	@Override
	public void generateBlocks(FlatRegion region, World w, TriangleList triangleList) {
		
		IntegerBoundingBox box = this.getBoundingBox().getIntersectionArea(new IntegerBoundingBox(region));
		if(!box.isValid()) return;
			
		this.generateOutline(region, w, triangleList);
	}
	
	
	protected void generateOutline(FlatRegion region, World w, TriangleList triangleList) {
		
		VMapElementStyle[] styles = VMapElementStyleSelector.getStyle(this);
		if(styles == null) return;
		for(VMapElementStyle style : styles) {
			if(style == null) continue; if(style.state == null) continue;
			
			LineGenerator lineGenerator = new LineGenerator.TerrainLine(
					(x, z) -> (int) Math.round(triangleList.interpolateHeight(x, z)) + style.y, 
					w, region, style.state
			);

			for (Vector2DH[] temp : vertices) {
				for (int i = 0; i < temp.length - 1; ++i) {
					lineGenerator.generate(temp[i], temp[i + 1]);
				}
				if (this.isClosed()) {
					lineGenerator.generate(temp[temp.length - 1], temp[0]);
				}
			}
		}
	}

	
	public Vector2DH[][] getVertexList() {
		return this.vertices;
	}
	
	
	public boolean isClosed() {
		return this.isClosed;
	}
	
	
	@Override
	public String toString() {
		return "VMapLineString{type=" + parent.getType() + ",vertexLen=" + vertices[0].length + "}";
	}
	
}