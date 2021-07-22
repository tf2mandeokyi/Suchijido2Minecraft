package com.mndk.kvm2m.core.vmap.elem.line;

import com.mndk.kvm2m.core.util.LineGenerator;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vmap.VMapElementStyleSelector;
import com.mndk.kvm2m.core.vmap.VMapElementStyleSelector.VMapElementStyle;
import com.mndk.kvm2m.core.vmap.elem.VMapLayer;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.world.World;

public class VMapContour extends VMapLineString {

	public final int elevation;
	
	public VMapContour(VMapLayer parent, Vector2DH[] vertices, Object[] rowData) {
		super(parent, new Vector2DH[][] { vertices }, rowData, false);
		this.elevation = (int) Math.round(((Number) this.getData("등고수치")).doubleValue());
		for(Vector2DH vs : vertices) {
			vs.height = elevation;
		}
	}
	
	protected void generateOutline(FlatRegion region, World w, TriangleList triangleList) {
		
		VMapElementStyle[] styles = VMapElementStyleSelector.getStyle(this);
		if(styles == null) return;
		for(VMapElementStyle style : styles) {
			if(style == null) continue; if(style.state == null) continue;
			
			LineGenerator lineGenerator = new LineGenerator.TerrainLine(
					(x, z) -> this.elevation, 
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
	
	
	@Override
	public String toString() {
		return "VMapContour{vertexLen=" + vertices[0].length + ",height=" + elevation + "}";
	}
}
