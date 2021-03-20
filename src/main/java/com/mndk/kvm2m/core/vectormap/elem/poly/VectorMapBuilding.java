package com.mndk.kvm2m.core.vectormap.elem.poly;

import com.mndk.kvm2m.core.projection.grs80.Grs80Projection;
import com.mndk.kvm2m.core.util.LineGenerator;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vectormap.VectorMapObjectType;
import com.mndk.kvm2m.core.vectormap.elem.IHasElevationData;
import com.mndk.ngiparser.ngi.element.NgiPolygonElement;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

public class VectorMapBuilding extends VectorMapPolyline implements IHasElevationData {

	private final int buildingHeight;
	
	private static final int FLOOR_HEIGHT = 4;
	
	public VectorMapBuilding(NgiPolygonElement polygon, Grs80Projection projection, VectorMapObjectType type) {
		super(polygon, projection, type);
		this.buildingHeight = (Integer) polygon.getRowData("층수") * FLOOR_HEIGHT;
	}

	@Override
	public int getElevation() {
		return this.buildingHeight;
	}
	
	
	
	public void generateBlocks(FlatRegion region, World w, TriangleList triangleList) {
		
		IBlockState state = this.getType().getBlockState();
		
		if(state == null) return;
		
		LineGenerator.world = w;
		LineGenerator.state = state;
		LineGenerator.region = region;
		LineGenerator.getYFunction = v -> {
			double height = triangleList.interpolateHeight(v);
			return (int) Math.round(height);
		};
		
		int maxHeight = this.getMaxTerrainHeight(triangleList) + this.buildingHeight;
		
		Vector2DH[][] vertexList = this.getVertexList();
		for(int j = 0; j < vertexList.length; ++j) {
			Vector2DH[] temp = vertexList[j];
        	for(int i = 0; i < temp.length - 1; ++i) {
        		LineGenerator.generateLineWithMaxHeight(temp[i], temp[i+1], maxHeight);
            }
        	if(this.isClosed()) {
                LineGenerator.generateLineWithMaxHeight(temp[temp.length-1], temp[0], maxHeight);
        	}
		}
	}
	
	
	
	private int getMaxTerrainHeight(TriangleList triangleList) {
		
		int yMax = -10000, yTemp;
		LineGenerator.getYFunction = v -> {
			double height = triangleList.interpolateHeight(v);
			return (int) Math.round(height);
		};
		
		Vector2DH[][] vertexList = this.getVertexList();
		for(int j = 0; j < vertexList.length; ++j) {
			Vector2DH[] temp = vertexList[j];
        	for(int i = 0; i < temp.length - 1; ++i) {
        		yMax = yMax < (yTemp = LineGenerator.getMaxHeightOfTheLine(temp[i], temp[i+1])) ? yTemp : yMax;
            }
        	if(this.isClosed()) {
        		yMax = yMax < (yTemp = LineGenerator.getMaxHeightOfTheLine(temp[temp.length-1], temp[0])) ? yTemp : yMax;
        	}
		}
		
		return yMax;
	}

}
