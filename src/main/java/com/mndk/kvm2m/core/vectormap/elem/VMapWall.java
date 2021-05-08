package com.mndk.kvm2m.core.vectormap.elem;

import java.util.Map;

import com.mndk.kvm2m.core.util.EdgeGenerator;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vectormap.VMapElementStyleSelector;
import com.mndk.kvm2m.core.vectormap.VMapElementStyleSelector.VMapElementStyle;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.world.World;

public class VMapWall extends VMapLine {

	
	public VMapWall(VMapElementLayer parent, Vector2DH[][] vertexes, Map<String, Object> dataRow, boolean isClosed) {
		super(parent, vertexes, dataRow, isClosed);
	}
	
	
	public VMapWall(VMapElementLayer parent, Vector2DH[][] vertexes, Object[] dataRow, boolean isClosed) {
		super(parent, vertexes, dataRow, isClosed);
	}
	
	
	protected void generateOutline(FlatRegion region, World w, TriangleList triangleList) {
		
		VMapElementStyle[] styles = VMapElementStyleSelector.getStyle(this);
		if(styles == null) return;
		for(VMapElementStyle style : styles) {
			if(style == null) continue; if(style.state == null) continue;
			
			EdgeGenerator lineGenerator = new EdgeGenerator.TerrainWall(
					(x, z) -> (int) Math.round(triangleList.interpolateHeight(x, z)), 
					w, region, style.state, style.y
			);
			
			for(int j = 0; j < vertexList.length; ++j) {
				Vector2DH[] temp = vertexList[j];
				for(int i = 0; i < temp.length - 1; ++i) {
					lineGenerator.generate(temp[i], temp[i+1]);
				}
				if(this.isClosed()) {
					lineGenerator.generate(temp[temp.length-1], temp[0]);
				}
			}
		}
	}

	

}
