package com.mndk.scjd2mc.core.scjd.elem.poly;

import com.mndk.scjd2mc.core.util.LineGenerator;
import com.mndk.scjd2mc.core.util.math.Vector2DH;
import com.mndk.scjd2mc.core.util.shape.BoundingBoxInteger;
import com.mndk.scjd2mc.core.util.shape.TriangleList;
import com.mndk.scjd2mc.core.scjd.type.ElementStyleSelector;
import com.mndk.scjd2mc.core.scjd.type.ElementStyleSelector.ScjdElementStyle;
import com.mndk.scjd2mc.core.scjd.SuchijidoUtils;
import com.mndk.scjd2mc.core.scjd.elem.ScjdLayer;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.FlatRegion;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

public class ScjdBuilding extends ScjdPolygon {
	
	
	public static final double FLOOR_HEIGHT = 3.5;
	
	
	public ScjdBuilding(ScjdLayer layer, String id, Vector2DH[][] polygon, Map<String, Object> dataRow) {
		super(layer, id, polygon, dataRow, true);
	}
	
	
	public ScjdBuilding(ScjdLayer layer, String id, Vector2DH[][] polygon, Object[] dataRow) {
		super(layer, id, polygon, dataRow, true);
	}
	
	
	@Override
	protected void generateOutline(FlatRegion region, World w, TriangleList triangleList) {
		
		ScjdElementStyle[] styles = ElementStyleSelector.getStyle(this);
		ScjdElementStyle style = styles != null ? styles[0] : null;
		if(style == null) return; if(style.state == null) return;
		
		int buildingHeight = style.y;
		
		int maxHeight = this.getMaxTerrainHeight(triangleList) + buildingHeight;
		
		LineGenerator lineGenerator = new LineGenerator.BuildingWall(
				(x, z) -> (int) Math.round(triangleList.interpolateHeight(x, z)),
				w, region, style.state, maxHeight
		);
		
		
		Vector2DH[][] vertexList = this.getVertexList();
		for (Vector2DH[] temp : vertexList) {
			for (int i = 0; i < temp.length - 1; ++i) {
				lineGenerator.generate(temp[i], temp[i + 1]);
			}
			if (this.shouldBeFilled()) {
				lineGenerator.generate(temp[temp.length - 1], temp[0]);
			}
		}
	}
	
	
	@Override
	protected void fillBlocks(FlatRegion region, World w, TriangleList triangleList, BoundingBoxInteger limitBox) {
		
		ScjdElementStyle[] styles = ElementStyleSelector.getStyle(this);
		ScjdElementStyle style = styles != null ? styles[0] : null;
		if(style == null) return; if(style.state == null) return;
		
		int buildingHeight = style.y;
		
		int y = this.getMaxTerrainHeight(triangleList) + buildingHeight - 1;
		
		for(int z = limitBox.zmin; z <= limitBox.zmax; ++z) {
			for(int x = limitBox.xmin; x <= limitBox.xmax; ++x) {
				if(!region.contains(new Vector(x, region.getMinimumY(), z)) || !this.containsPoint(new Vector2DH(x+.5, z+.5))) continue;
				SuchijidoUtils.setBlock(w, new BlockPos(x, y, z), style.state);
			}
		}
	}
	
	
	private int getMaxTerrainHeight(TriangleList triangleList) {
		
		int yMax = -10000, yTemp;
		LineGenerator.MeasureHeight heightMeasurer = new LineGenerator.MeasureHeight(
				(x, z) -> (int) Math.round(triangleList.interpolateHeight(x, z))
		);
		
		Vector2DH[][] vertexList = this.getVertexList();
		for (Vector2DH[] temp : vertexList) {
			for (int i = 0; i < temp.length - 1; ++i) {
				yMax = yMax < (yTemp = heightMeasurer.getMaxHeight(temp[i], temp[i + 1])) ? yTemp : yMax;
			}
			if (this.shouldBeFilled()) {
				yMax = yMax < (yTemp = heightMeasurer.getMaxHeight(temp[temp.length - 1], temp[0])) ? yTemp : yMax;
			}
		}
		
		return yMax;
	}
	
	
	@Override
	public String toString() {
		if(!Bootstrap.isRegistered()) {
			return "VMapBuilding{vertexLen=" + vertices[0].length + ",floor=" + this.getData("층수") + "}";
		}
		ScjdElementStyle[] styles = ElementStyleSelector.getStyle(this);
		assert styles != null;
		return "VMapBuilding{vertexLen=" + vertices[0].length + ",height=" + styles[0].y + "}";
	}

}
