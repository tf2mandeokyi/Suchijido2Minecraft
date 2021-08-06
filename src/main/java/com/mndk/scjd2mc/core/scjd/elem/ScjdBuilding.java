package com.mndk.scjd2mc.core.scjd.elem;

import com.mndk.scjd2mc.core.scjd.SuchijidoUtils;
import com.mndk.scjd2mc.core.scjd.geometry.LineString;
import com.mndk.scjd2mc.core.scjd.geometry.Polygon;
import com.mndk.scjd2mc.core.scjd.type.ElementStyleSelector;
import com.mndk.scjd2mc.core.scjd.type.ElementStyleSelector.ScjdElementStyle;
import com.mndk.scjd2mc.core.util.LineGenerator;
import com.mndk.scjd2mc.core.util.math.Vector2DH;
import com.mndk.scjd2mc.core.util.shape.BoundingBoxInteger;
import com.mndk.scjd2mc.core.util.shape.TriangleList;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.FlatRegion;
import lombok.Getter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

public class ScjdBuilding extends ScjdElement<Polygon> {
	
	
	public static final double FLOOR_HEIGHT = 3.5;


	@Getter private final int floorCount;
	@Getter private final boolean generateShell;
	
	
	public ScjdBuilding(ScjdLayer layer, String id, Polygon polygon, Object[] dataRow, boolean generateShell) {
		super(layer, id, polygon, dataRow);
		this.floorCount = this.parseFloorCount();
		this.generateShell = generateShell;
	}


	private int parseFloorCount() {
		Object floorCount = this.getData("층수");
		if(floorCount instanceof Integer) {
			return (Integer) floorCount;
		}
		else {
			return (int) Math.round(((Number) floorCount).doubleValue());
		}
	}
	

	public void generateBlocks(FlatRegion region, World w, TriangleList triangleList) {

		ScjdElementStyle[] styles = ElementStyleSelector.getStyle(this);
		ScjdElementStyle style = styles != null ? styles[0] : null;
		if(style == null) return; if(style.state == null) return;

		int maxHeight = this.getMaxTerrainHeight(triangleList) + (int) Math.round(floorCount * FLOOR_HEIGHT);
		
		LineGenerator lineGenerator = new LineGenerator.BuildingWall(
				(x, z) -> (int) Math.round(triangleList.interpolateHeight(x, z)),
				w, region, style.state, maxHeight
		);

		LineString[] vertexList = shape.getShape();
		for (LineString lineString : vertexList) {
			Vector2DH[] temp = lineString.getShape();
			for (int i = 0; i < temp.length - 1; ++i) {
				lineGenerator.generate(temp[i], temp[i + 1]);
			}
			if (!temp[temp.length - 1].equalsXZ(temp[0])) {
				lineGenerator.generate(temp[temp.length - 1], temp[0]);
			}
		}

		BoundingBoxInteger bbox = shape.getBoundingBox().toMaximumBoundingBoxInteger();
		for(int z = bbox.zmin; z <= bbox.zmax; ++z) {
			for(int x = bbox.xmin; x <= bbox.xmax; ++x) {
				if(!region.contains(new Vector(x, region.getMinimumY(), z)) ||
						!shape.containsPoint(new Vector2DH(x+.5, z+.5))) continue;

				SuchijidoUtils.setBlock(w, new BlockPos(x, maxHeight - 1, z), style.state);
			}
		}
	}
	
	
	private int getMaxTerrainHeight(TriangleList triangleList) {
		
		int yMax = -10000, yTemp;
		LineGenerator.MeasureHeight heightMeasurer = new LineGenerator.MeasureHeight(
				(x, z) -> (int) Math.round(triangleList.interpolateHeight(x, z))
		);

		LineString[] vertexList = shape.getShape();
		for (LineString lineString : vertexList) {
			Vector2DH[] temp = lineString.getShape();
			for (int i = 0; i < temp.length - 1; ++i) {
				yMax = yMax < (yTemp = heightMeasurer.getMaxHeight(temp[i], temp[i + 1])) ? yTemp : yMax;
			}
			if (!temp[temp.length - 1].equalsXZ(temp[0])) {
				yMax = yMax < (yTemp = heightMeasurer.getMaxHeight(temp[temp.length - 1], temp[0])) ? yTemp : yMax;
			}
		}
		
		return yMax;
	}
	

	@Override
	public String toString() {
		return "VMapBuilding{floorCount=" + this.floorCount + "}";
	}

}
