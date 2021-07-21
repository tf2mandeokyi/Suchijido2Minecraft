package com.mndk.kvm2m.core.vmap.elem.poly;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.math.VectorMath;
import com.mndk.kvm2m.core.util.shape.BoundingBoxInteger;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vmap.VMapElementGeomType;
import com.mndk.kvm2m.core.vmap.VMapElementStyleSelector;
import com.mndk.kvm2m.core.vmap.VMapElementStyleSelector.VMapElementStyle;
import com.mndk.kvm2m.core.vmap.VMapUtils;
import com.mndk.kvm2m.core.vmap.elem.VMapLayer;
import com.mndk.kvm2m.core.vmap.elem.line.VMapLineString;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.FlatRegion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

public class VMapPolygon extends VMapLineString {

	
	private final boolean doFill;

	
	private VMapPolygon(VMapLayer parent, Map<String, Object> dataRow, boolean doFill) {
		super(parent, dataRow, true);
		this.geometryType = VMapElementGeomType.POLYGON;
		this.doFill = doFill;
	}

	
	private VMapPolygon(VMapLayer parent, Object[] dataRow, boolean doFill) {
		super(parent, dataRow, true);
		this.geometryType = VMapElementGeomType.POLYGON;
		this.doFill = doFill;
	}
	
	
	public VMapPolygon(VMapLayer parent, Vector2DH[][] vertices, Map<String, Object> dataRow, boolean doFill) {
		this(parent, dataRow, doFill);
		this.vertices = vertices;
		this.setupBoundingBox();
	}
	
	
	public VMapPolygon(VMapLayer parent, Vector2DH[][] vertices, Object[] dataRow, boolean doFill) {
		this(parent, dataRow, doFill);
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
	
	
	public VMapPolygon merge(VMapPolygon other) {
		Vector2DH[][] newVertexList = new Vector2DH[this.vertices.length + other.vertices.length][];
		System.arraycopy(this.vertices, 0, newVertexList, 0, this.vertices.length);
		System.arraycopy(other.vertices, 0, newVertexList, this.vertices.length, other.vertices.length);
		
		return new VMapPolygon(this.parent, newVertexList, this.dataRow, this.doFill);
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
		
		VMapElementStyle[] styles = VMapElementStyleSelector.getStyle(this);
		if(styles == null) return;
		for(VMapElementStyle style : styles) {
			if(style == null) continue; if(style.state == null) continue;
			
			for(int z = limitBox.zmin; z <= limitBox.zmax; ++z) {
				for(int x = limitBox.xmin; x <= limitBox.xmax; ++x) {
					if(!region.contains(new Vector(x, region.getMinimumY(), z)) || !this.containsPoint(new Vector2DH(x+.5, z+.5))) continue;

					int y = (int) Math.round(triangleList.interpolateHeight(x, z)) + style.y;

					VMapUtils.setBlock(w, new BlockPos(x, y, z), style.state);
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
