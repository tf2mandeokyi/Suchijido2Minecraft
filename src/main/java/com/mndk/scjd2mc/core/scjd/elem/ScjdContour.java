package com.mndk.scjd2mc.core.scjd.elem;

import com.mndk.scjd2mc.core.scjd.geometry.LineString;
import com.mndk.scjd2mc.core.scjd.type.ElementStyleSelector;
import com.mndk.scjd2mc.core.scjd.type.ElementStyleSelector.ScjdElementStyle;
import com.mndk.scjd2mc.core.util.LineGenerator;
import com.mndk.scjd2mc.core.util.math.Vector2DH;
import com.mndk.scjd2mc.core.util.shape.TriangleList;
import com.sk89q.worldedit.regions.FlatRegion;
import net.minecraft.world.World;

public class ScjdContour extends ScjdElement<LineString> {

	public final int elevation;
	
	public ScjdContour(ScjdLayer parent, LineString lineString, Object[] rowData) {
		super(parent, "", lineString, rowData);
		this.elevation = (int) Math.round(((Number) this.getData("등고수치")).doubleValue());
		for(Vector2DH vs : lineString.getShape()) {
			vs.height = elevation;
		}
	}


	@Override
	public void generateBlocks(FlatRegion region, World w, TriangleList triangleList) {
		
		ScjdElementStyle[] styles = ElementStyleSelector.getStyle(this);
		if(styles == null) return;
		for(ScjdElementStyle style : styles) {
			if(style == null) continue; if(style.state == null) continue;
			
			LineGenerator lineGenerator = new LineGenerator.TerrainLine(
					(x, z) -> this.elevation, 
					w, region, style.state
			);

			Vector2DH[] temp = shape.getShape();
			for (int i = 0; i < temp.length - 1; ++i) {
				lineGenerator.generate(temp[i], temp[i + 1]);
			}
			if (shape.isClosed()) {
				lineGenerator.generate(temp[temp.length - 1], temp[0]);
			}
		}
	}
	

	@Override
	public String toString() {
		return "VMapContour{vertexLen=" + shape.size() + ",height=" + elevation + "}";
	}
}
