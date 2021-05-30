package com.mndk.kvm2m.core.vmap.elem.poly;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.math.VectorMath;
import com.mndk.kvm2m.core.util.shape.IntegerBoundingBox;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vmap.VMapElementStyleSelector;
import com.mndk.kvm2m.core.vmap.VMapElementStyleSelector.VMapElementStyle;
import com.mndk.kvm2m.core.vmap.elem.VMapElementLayer;
import com.mndk.kvm2m.core.vmap.elem.line.VMapPolyline;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.FlatRegion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

public class VMapPolygon extends VMapPolyline {

	
	private final boolean doFill;

	
	private VMapPolygon(VMapElementLayer parent, Map<String, Object> dataRow, boolean doFill) {
		super(parent, dataRow, true);
		this.doFill = doFill;
	}

	
	private VMapPolygon(VMapElementLayer parent, Object[] dataRow, boolean doFill) {
		super(parent, dataRow, true);
		this.doFill = doFill;
	}
	
	
	public VMapPolygon(VMapElementLayer parent, Vector2DH[][] vertices, Map<String, Object> dataRow, boolean doFill) {
		this(parent, dataRow, doFill);
		this.vertices = vertices;
		this.getBoundingBox();
	}
	
	
	public VMapPolygon(VMapElementLayer parent, Vector2DH[][] vertices, Object[] dataRow, boolean doFill) {
		this(parent, dataRow, doFill);
		this.vertices = vertices;
		this.getBoundingBox();
	}
	
	
	public boolean containsPoint(Vector2DH point) {

		for (Vector2DH[] vertex : vertices) {
			boolean inside = false;
			for (int i = 0, j = vertex.length - 1; i < vertex.length; j = i++) {
				if (VectorMath.checkRayXIntersection(point, vertex[i], vertex[j])) {
					inside = !inside;
				}
			}
			if (inside) return true;
		}
		return false;
	}
	
	
	public VMapPolygon merge(VMapPolygon other) {
		Vector2DH[][] newVertexList = new Vector2DH[this.vertices.length + other.vertices.length][];
		System.arraycopy(this.vertices, 0, newVertexList, 0, this.vertices.length);
		System.arraycopy(other.vertices, 0, newVertexList, this.vertices.length, other.vertices.length);
		
		return new VMapPolygon(this.parent, newVertexList, this.dataRow, this.doFill);
	}
	
	
	@Override
	public final void generateBlocks(FlatRegion region, World w, TriangleList triangleList) {
		
		IntegerBoundingBox box = this.getBoundingBox().getIntersectionArea(new IntegerBoundingBox(region));
		if(!box.isValid()) return;
		
		this.generateOutline(region, w, triangleList);
		
		if(this.shouldBeFilled()) {
			this.fillBlocks(region, w, triangleList, box);
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
	
	
	public boolean shouldBeFilled() {
		return doFill;
	}
	
	
	@Override
	public String toString() {
		return "VMapPolygon{type=" + parent.getType() + ",vertexLen=" + vertices[0].length + "}";
	}
	
}
