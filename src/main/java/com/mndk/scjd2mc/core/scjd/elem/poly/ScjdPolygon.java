package com.mndk.scjd2mc.core.scjd.elem.poly;

import com.mndk.scjd2mc.core.scjd.SuchijidoUtils;
import com.mndk.scjd2mc.core.scjd.elem.ScjdLayer;
import com.mndk.scjd2mc.core.scjd.elem.line.ScjdLineString;
import com.mndk.scjd2mc.core.scjd.type.ElementGeometryType;
import com.mndk.scjd2mc.core.scjd.type.ElementStyleSelector;
import com.mndk.scjd2mc.core.scjd.type.ElementStyleSelector.ScjdElementStyle;
import com.mndk.scjd2mc.core.util.math.Vector2DH;
import com.mndk.scjd2mc.core.util.math.VectorMath;
import com.mndk.scjd2mc.core.util.shape.BoundingBoxInteger;
import com.mndk.scjd2mc.core.util.shape.TriangleList;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.FlatRegion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScjdPolygon extends ScjdLineString {

	
	private final boolean doFill;

	
	private ScjdPolygon(ScjdLayer parent, String id, Map<String, Object> dataRow, boolean doFill) {
		super(parent, id, dataRow, true);
		this.geometryType = ElementGeometryType.POLYGON;
		this.doFill = doFill;
	}

	
	private ScjdPolygon(ScjdLayer parent, String id, Object[] dataRow, boolean doFill) {
		super(parent, id, dataRow, true);
		this.geometryType = ElementGeometryType.POLYGON;
		this.doFill = doFill;
	}
	
	
	public ScjdPolygon(ScjdLayer parent, String id, Vector2DH[][] vertices, Map<String, Object> dataRow, boolean doFill) {
		this(parent, id, dataRow, doFill);
		this.vertices = vertices;
		this.setupBoundingBox();
	}
	
	
	public ScjdPolygon(ScjdLayer parent, String id, Vector2DH[][] vertices, Object[] dataRow, boolean doFill) {
		this(parent, id, dataRow, doFill);
		this.vertices = vertices;
		this.setupBoundingBox();
	}
	
	
	public boolean containsPoint(Vector2DH point) {

		boolean result = false;

		for (Vector2DH[] vertex : vertices) {
			boolean inside = false;
			for (int i = 0, j = vertex.length - 1; i < vertex.length; j = i++) {
				if (VectorMath.checkRayXIntersection(point, vertex[i], vertex[j])) {
					inside = !inside;
				}
			}
			if (inside) {
				result = !result;
			}
		}
		return result;
	}
	
	
	public ScjdPolygon merge(ScjdPolygon other) {
		Vector2DH[][] newVertexList = new Vector2DH[this.vertices.length + other.vertices.length][];
		System.arraycopy(this.vertices, 0, newVertexList, 0, this.vertices.length);
		System.arraycopy(other.vertices, 0, newVertexList, this.vertices.length, other.vertices.length);
		
		return new ScjdPolygon(this.parent, this.id, newVertexList, this.dataRow, this.doFill);
	}


	@Override
	protected Map<String, Object> getSerializableMapGeometryData() {
		Map<String, Object> result = new HashMap<>();
		result.put("type", "Polygon");
		List<List<List<Double>>> polygon = new ArrayList<>();
		for(Vector2DH[] l : this.vertices) {
			List<List<Double>> line = new ArrayList<>();
			for(Vector2DH v : l) {
				line.add(v.toRoundDoubleList());
			}
			polygon.add(line);
		}
		result.put("coordinates", polygon);
		return result;
	}


	@Override
	public final void generateBlocks(FlatRegion region, World w, TriangleList triangleList) {
		
		BoundingBoxInteger box = this.getBoundingBoxInteger().getIntersectionArea(new BoundingBoxInteger(region));
		if(!box.isValid()) return;
		
		this.generateOutline(region, w, triangleList);
		
		if(this.shouldBeFilled()) {
			this.fillBlocks(region, w, triangleList, box);
		}
	}
	
	
	protected void fillBlocks(FlatRegion region, World w, TriangleList triangleList, BoundingBoxInteger limitBox) {
		
		ScjdElementStyle[] styles = ElementStyleSelector.getStyle(this);
		if(styles == null) return;
		for(ScjdElementStyle style : styles) {
			if(style == null) continue; if(style.state == null) continue;
			
			for(int z = limitBox.zmin; z <= limitBox.zmax; ++z) {
				for(int x = limitBox.xmin; x <= limitBox.xmax; ++x) {
					if(!region.contains(new Vector(x, region.getMinimumY(), z)) || !this.containsPoint(new Vector2DH(x+.5, z+.5))) continue;

					int y = (int) Math.round(triangleList.interpolateHeight(x, z)) + style.y;

					SuchijidoUtils.setBlock(w, new BlockPos(x, y, z), style.state);
				}
			}
		}
	}
	
	
	public boolean shouldBeFilled() {
		return doFill;
	}
	
	
	@Override
	public String toString() {
		return "VMapPolygon{type=" + parent.getType() + ",vertexLen=" + vertices[0].length + "}";
	}
	
}
