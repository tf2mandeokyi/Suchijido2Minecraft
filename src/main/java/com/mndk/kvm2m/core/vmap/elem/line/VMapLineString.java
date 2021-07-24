package com.mndk.kvm2m.core.vmap.elem.line;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mndk.kvm2m.core.util.LineGenerator;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.shape.BoundingBoxDouble;
import com.mndk.kvm2m.core.util.shape.BoundingBoxInteger;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vmap.elem.VMapElement;
import com.mndk.kvm2m.core.vmap.elem.VMapElementStyleSelector;
import com.mndk.kvm2m.core.vmap.elem.VMapElementStyleSelector.VMapElementStyle;
import com.mndk.kvm2m.core.vmap.elem.VMapLayer;
import com.mndk.kvm2m.core.vmap.type.VMapElementGeomType;
import com.sk89q.worldedit.regions.FlatRegion;
import net.minecraft.world.World;

import java.util.Map;

public class VMapLineString extends VMapElement {

	
	protected Vector2DH[][] vertices;
	private final boolean isClosed;

	
	protected VMapLineString(VMapLayer parent, String id, Map<String, Object> dataRow, boolean isClosed) {
		super(parent, id, dataRow, VMapElementGeomType.LINESTRING);
		this.isClosed = isClosed;
	}

	
	protected VMapLineString(VMapLayer parent, String id, Object[] dataRow, boolean isClosed) {
		super(parent, id, dataRow, VMapElementGeomType.LINESTRING);
		this.isClosed = isClosed;
	}
	
	
	public VMapLineString(VMapLayer parent, String id, Vector2DH[][] vertices, Map<String, Object> dataRow, boolean isClosed) {
		this(parent, id, dataRow, isClosed);
		this.vertices = vertices;
		this.setupBoundingBox();
	}
	
	
	public VMapLineString(VMapLayer parent, String id, Vector2DH[][] vertices, Object[] dataRow, boolean isClosed) {
		this(parent, id, dataRow, isClosed);
		this.vertices = vertices;
		this.setupBoundingBox();
	}


	public BoundingBoxInteger getBoundingBoxInteger() {
		return bbox.toMaximumBoundingBoxInteger();
	}


	protected void setupBoundingBox() {
		double minX = Double.MAX_VALUE, minZ = Double.MAX_VALUE, maxX = Double.MIN_VALUE, maxZ = Double.MIN_VALUE;
		for(Vector2DH[] va : vertices) for(Vector2DH v : va) {
			if(v.x < minX) minX = v.x;
			if(v.x > maxX) maxX = v.x;
			if(v.z < minZ) minZ = v.z;
			if(v.z > maxZ) maxZ = v.z;
		}
		this.bbox = new BoundingBoxDouble(minX, minZ, maxX, maxZ);
	}

	
	@Override
	public void generateBlocks(FlatRegion region, World w, TriangleList triangleList) {
		
		BoundingBoxInteger box = this.getBoundingBoxInteger().getIntersectionArea(new BoundingBoxInteger(region));
		if(!box.isValid()) return;
			
		this.generateOutline(region, w, triangleList);
	}


	@Override
	protected JsonObject getJsonGeometryData() {
		JsonObject result = new JsonObject();
		result.addProperty("type", "LineString");
		JsonArray line = new JsonArray();
		for(Vector2DH p : this.vertices[0]) {
			line.add(p.toJsonArray());
		}
		result.add("coordinates", line);
		return result;
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