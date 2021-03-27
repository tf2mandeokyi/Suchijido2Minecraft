package com.mndk.kvm2m.core.vectormap.elem.poly;

import com.mndk.kvm2m.core.projection.Grs80Projection;
import com.mndk.kvm2m.core.util.LineGenerator;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vectormap.VMapObjectType;
import com.mndk.kvm2m.core.vectormap.elem.IHasElevationData;
import com.mndk.ngiparser.ngi.element.NgiPolygonElement;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

public class VMapBuilding extends VMapPolyline implements IHasElevationData {
	
	
	private static final int FLOOR_HEIGHT = 4;

	
	private final int buildingHeight;
	
	
	public VMapBuilding(NgiPolygonElement polygon, Grs80Projection projection, VMapObjectType type) {
		super(polygon, projection, type);
		if(type == VMapObjectType.건물) {
			this.buildingHeight = (Integer) polygon.getRowData("층수") * FLOOR_HEIGHT;
		}
		else {
			this.buildingHeight = 0;
		}
	}

	
	@Override
	public int getElevation() {
		return this.buildingHeight;
	}
	
	
	@Override
	protected void generateOutline(FlatRegion region, World w, TriangleList triangleList) {
		
		IBlockState state = this.getType().getBlockState();
		
		if(state == null) return;
		
		LineGenerator lineGenerator = new LineGenerator(
				v -> (int) Math.round(triangleList.interpolateHeight(v)),
				w, region, state
		);
		
		int maxHeight = this.getMaxTerrainHeight(triangleList) + this.buildingHeight;
		
		Vector2DH[][] vertexList = this.getVertexList();
		for(int j = 0; j < vertexList.length; ++j) {
			Vector2DH[] temp = vertexList[j];
        	for(int i = 0; i < temp.length - 1; ++i) {
        		lineGenerator.generateLineWithMaxHeight(temp[i], temp[i+1], maxHeight);
            }
        	if(this.isClosed()) {
        		lineGenerator.generateLineWithMaxHeight(temp[temp.length-1], temp[0], maxHeight);
        	}
		}
	}
	
	
	@Override
	protected int getHeightValueOfPoint(Vector2DH v, TriangleList triangleList) {
		return this.getMaxTerrainHeight(triangleList) + this.buildingHeight - 1;
	}
	
	
	
	private int getMaxTerrainHeight(TriangleList triangleList) {
		
		int yMax = -10000, yTemp;
		LineGenerator lineGenerator = new LineGenerator(
				v -> (int) Math.round(triangleList.interpolateHeight(v)), 
				null, null, null
		);
		
		Vector2DH[][] vertexList = this.getVertexList();
		for(int j = 0; j < vertexList.length; ++j) {
			Vector2DH[] temp = vertexList[j];
        	for(int i = 0; i < temp.length - 1; ++i) {
        		yMax = yMax < (yTemp = lineGenerator.getMaxHeightOfTheLine(temp[i], temp[i+1])) ? yTemp : yMax;
            }
        	if(this.isClosed()) {
        		yMax = yMax < (yTemp = lineGenerator.getMaxHeightOfTheLine(temp[temp.length-1], temp[0])) ? yTemp : yMax;
        	}
		}
		
		return yMax;
	}

}
