package com.mndk.kvm2m.core.vmap.elem.line;

import com.mndk.kvm2m.core.util.LineGenerator;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vmap.VMapElementStyleSelector;
import com.mndk.kvm2m.core.vmap.VMapElementStyleSelector.VMapElementStyle;
import com.mndk.kvm2m.core.vmap.elem.VMapElementLayer;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.world.World;

public class VMapContour extends VMapPolyline {

	public final int elevation;
	
	public VMapContour(VMapElementLayer parent, Vector2DH[] vertices, Object[] rowData) {
		super(parent, new Vector2DH[][] { vertices }, rowData, false);
		this.elevation = (int) Math.round((Double) this.getDataByColumn("등고수치"));
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
			
			for(int j = 0; j < vertices.length; ++j) {
				Vector2DH[] temp = vertices[j];
				for(int i = 0; i < temp.length - 1; ++i) {
					lineGenerator.generate(temp[i], temp[i+1]);
				}
				if(this.isClosed()) {
					lineGenerator.generate(temp[temp.length-1], temp[0]);
				}
			}
		}
	}
	
	
	@Override
	public String toString() {
		return "VMapContour{vertexLen=" + vertices[0].length + ",height=" + elevation + "}";
	}
}
