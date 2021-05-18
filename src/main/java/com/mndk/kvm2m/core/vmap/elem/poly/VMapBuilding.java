package com.mndk.kvm2m.core.vmap.elem.poly;

import java.util.Map;

import com.mndk.kvm2m.core.util.LineGenerator;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.shape.IntegerBoundingBox;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vmap.VMapElementStyleSelector;
import com.mndk.kvm2m.core.vmap.VMapElementStyleSelector.VMapElementStyle;
import com.mndk.kvm2m.core.vmap.elem.VMapElementLayer;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VMapBuilding extends VMapPolygon {
	
	
	public static final int FLOOR_HEIGHT = 4;
	
	
	public VMapBuilding(VMapElementLayer layer, Vector2DH[][] polygon, Map<String, Object> dataRow) {
		super(layer, polygon, dataRow, true);
	}
	
	
	public VMapBuilding(VMapElementLayer layer, Vector2DH[][] polygon, Object[] dataRow) {
		super(layer, polygon, dataRow, true);
	}
	
	
	@Override
	protected void generateOutline(FlatRegion region, World w, TriangleList triangleList) {
		
		VMapElementStyle[] styles = VMapElementStyleSelector.getStyle(this);
		VMapElementStyle style = styles != null ? styles[0] : null;
		if(style == null) return; if(style.state == null) return;
		
		int buildingHeight = style.y;
		
		int maxHeight = this.getMaxTerrainHeight(triangleList) + buildingHeight;
		
		LineGenerator lineGenerator = new LineGenerator.BuildingWall(
				(x, z) -> (int) Math.round(triangleList.interpolateHeight(x, z)),
				w, region, style.state, maxHeight
		);
		
		
		Vector2DH[][] vertexList = this.getVertexList();
		for(int j = 0; j < vertexList.length; ++j) {
			Vector2DH[] temp = vertexList[j];
			for(int i = 0; i < temp.length - 1; ++i) {
				lineGenerator.generate(temp[i], temp[i+1]);
			}
			if(this.shouldBeFilled()) {
				lineGenerator.generate(temp[temp.length-1], temp[0]);
			}
		}
	}
	
	
	@Override
	protected void fillBlocks(FlatRegion region, World w, TriangleList triangleList, IntegerBoundingBox limitBox) {
		
		VMapElementStyle[] styles = VMapElementStyleSelector.getStyle(this);
		VMapElementStyle style = styles != null ? styles[0] : null;
		if(style == null) return; if(style.state == null) return;
		
		int buildingHeight = style.y;
		
		int y = this.getMaxTerrainHeight(triangleList) + buildingHeight - 1;
		
		for(int z = limitBox.zmin; z <= limitBox.zmax; ++z) {
			for(int x = limitBox.xmin; x <= limitBox.xmax; ++x) {
				if(!region.contains(new Vector(x, region.getMinimumY(), z)) || !this.containsPoint(new Vector2DH(x+.5, z+.5))) continue;
				w.setBlockState(new BlockPos(x, y, z), style.state);
			}
		}
	}
	
	
	private int getMaxTerrainHeight(TriangleList triangleList) {
		
		int yMax = -10000, yTemp;
		LineGenerator.MeasureHeight heightMeasurer = new LineGenerator.MeasureHeight(
				(x, z) -> (int) Math.round(triangleList.interpolateHeight(x, z))
		);
		
		Vector2DH[][] vertexList = this.getVertexList();
		for(int j = 0; j < vertexList.length; ++j) {
			Vector2DH[] temp = vertexList[j];
			for(int i = 0; i < temp.length - 1; ++i) {
				yMax = yMax < (yTemp = heightMeasurer.getMaxHeight(temp[i], temp[i+1])) ? yTemp : yMax;
			}
			if(this.shouldBeFilled()) {
				yMax = yMax < (yTemp = heightMeasurer.getMaxHeight(temp[temp.length-1], temp[0])) ? yTemp : yMax;
			}
		}
		
		return yMax;
	}
	
	
	@Override
	public String toString() {
		return "VMapBuilding{vertexLen=" + vertices[0].length + ",height=" + VMapElementStyleSelector.getStyle(this)[0].y + "}";
	}

}
