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
	
	public VectorMapBuilding(NgiPolygonElement polygon, Grs80Projection projection, VectorMapObjectType type) {
		super(polygon, projection, type);
		this.buildingHeight = (Integer) polygon.getRowData("층수");
	}

	@Override
	public int getElevation() {
		return this.buildingHeight;
	}
	
	
	
	public void generateBlocks(FlatRegion region, World w, TriangleList triangleList) {
		
		int y = this.getType().getDefaultHeight();
		IBlockState state = this.getType().getBlockState();
		
		if(state == null) return;
		
		LineGenerator.world = w;
		LineGenerator.state = state;
		LineGenerator.region = region;
		LineGenerator.getYFunction = v -> {
			double height = triangleList.interpolateHeight(v);
			return (int) Math.round(height) + y;
		};
		
		Vector2DH[][] vertexList = this.getVertexList();
		for(int j = 0; j < vertexList.length; ++j) {
			Vector2DH[] temp = vertexList[j];
        	for(int i = 0; i < temp.length - 1; ++i) {
        		LineGenerator.generateLine(temp[i], temp[i+1]);
            }
        	if(this.isClosed()) {
                LineGenerator.generateLine(temp[temp.length-1], temp[0]);
        	}
		}
	}

}
