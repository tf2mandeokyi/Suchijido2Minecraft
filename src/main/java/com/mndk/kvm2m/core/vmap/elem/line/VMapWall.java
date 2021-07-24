package com.mndk.kvm2m.core.vmap.elem.line;

import com.mndk.kvm2m.core.util.LineGenerator;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vmap.elem.VMapElementStyleSelector;
import com.mndk.kvm2m.core.vmap.elem.VMapElementStyleSelector.VMapElementStyle;
import com.mndk.kvm2m.core.vmap.elem.VMapLayer;
import com.sk89q.worldedit.regions.FlatRegion;
import net.minecraft.init.Bootstrap;
import net.minecraft.world.World;

import java.util.Map;

public class VMapWall extends VMapLineString {

	
	public VMapWall(VMapLayer parent, String id, Vector2DH[][] vertices, Map<String, Object> dataRow, boolean isClosed) {
		super(parent, id, vertices, dataRow, isClosed);
	}
	
	
	public VMapWall(VMapLayer parent, String id, Vector2DH[][] vertices, Object[] dataRow, boolean isClosed) {
		super(parent, id, vertices, dataRow, isClosed);
	}
	
	
	protected void generateOutline(FlatRegion region, World w, TriangleList triangleList) {
		
		VMapElementStyle[] styles = VMapElementStyleSelector.getStyle(this);
		if(styles == null) return;
		for(VMapElementStyle style : styles) {
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
		VMapElementStyle[] styles = VMapElementStyleSelector.getStyle(this);
		assert styles != null;
		return "VMapWall{type=" + parent.getType() + ",vertexLen=" + vertices[0].length + ",height=" + styles[0].y + "}";
	}

}
