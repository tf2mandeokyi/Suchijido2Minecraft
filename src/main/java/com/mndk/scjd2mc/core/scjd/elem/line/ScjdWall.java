package com.mndk.scjd2mc.core.scjd.elem.line;

import com.mndk.scjd2mc.core.util.LineGenerator;
import com.mndk.scjd2mc.core.util.math.Vector2DH;
import com.mndk.scjd2mc.core.util.shape.TriangleList;
import com.mndk.scjd2mc.core.scjd.type.ElementStyleSelector;
import com.mndk.scjd2mc.core.scjd.type.ElementStyleSelector.ScjdElementStyle;
import com.mndk.scjd2mc.core.scjd.elem.ScjdLayer;
import com.sk89q.worldedit.regions.FlatRegion;
import net.minecraft.init.Bootstrap;
import net.minecraft.world.World;

import java.util.Map;

public class ScjdWall extends ScjdLineString {

	
	public ScjdWall(ScjdLayer parent, String id, Vector2DH[][] vertices, Map<String, Object> dataRow, boolean isClosed) {
		super(parent, id, vertices, dataRow, isClosed);
	}
	
	
	public ScjdWall(ScjdLayer parent, String id, Vector2DH[][] vertices, Object[] dataRow, boolean isClosed) {
		super(parent, id, vertices, dataRow, isClosed);
	}
	
	
	protected void generateOutline(FlatRegion region, World w, TriangleList triangleList) {
		
		ScjdElementStyle[] styles = ElementStyleSelector.getStyle(this);
		if(styles == null) return;
		for(ScjdElementStyle style : styles) {
			if(style == null) continue; if(style.state == null) continue;
			
			LineGenerator lineGenerator = new LineGenerator.TerrainWall(
					(x, z) -> (int) Math.round(triangleList.interpolateHeight(x, z)), 
					w, region, style.state, style.y
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
		if(!Bootstrap.isRegistered()) {
			return "VMapWall{type=" + parent.getType() + ",vertexLen=" + vertices[0].length + "}";
		}
		ScjdElementStyle[] styles = ElementStyleSelector.getStyle(this);
		assert styles != null;
		return "VMapWall{type=" + parent.getType() + ",vertexLen=" + vertices[0].length + ",height=" + styles[0].y + "}";
	}

}
