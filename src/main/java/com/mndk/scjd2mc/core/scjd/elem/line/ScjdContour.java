package com.mndk.scjd2mc.core.scjd.elem.line;

import com.mndk.scjd2mc.core.util.LineGenerator;
import com.mndk.scjd2mc.core.util.math.Vector2DH;
import com.mndk.scjd2mc.core.util.shape.TriangleList;
import com.mndk.scjd2mc.core.scjd.type.ElementStyleSelector;
import com.mndk.scjd2mc.core.scjd.type.ElementStyleSelector.ScjdElementStyle;
import com.mndk.scjd2mc.core.scjd.elem.ScjdLayer;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.world.World;

public class ScjdContour extends ScjdLineString {

	public final int elevation;
	
	public ScjdContour(ScjdLayer parent, Vector2DH[] vertices, Object[] rowData) {
		super(parent, "", new Vector2DH[][] { vertices }, rowData, false);
		this.elevation = (int) Math.round(((Number) this.getData("등고수치")).doubleValue());
		for(Vector2DH vs : vertices) {
			vs.height = elevation;
		}
	}
	
	protected void generateOutline(FlatRegion region, World w, TriangleList triangleList) {
		
		ScjdElementStyle[] styles = ElementStyleSelector.getStyle(this);
		if(styles == null) return;
		for(ScjdElementStyle style : styles) {
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
