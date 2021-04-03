package com.mndk.kvm2m.core.vectormap.elem.poly;

import java.util.Map;

import com.mndk.kvm2m.core.util.LineGenerator;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.shape.BoundingBox;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vectormap.VMapBlockSelector;
import com.mndk.kvm2m.core.vectormap.elem.VMapElementLayer;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VMapBuilding extends VMapPolyline {
	
	
	public static final int FLOOR_HEIGHT = 4;
	
	
	public VMapBuilding(VMapElementLayer layer, Vector2DH[][] polygon, Map<String, Object> dataRow) {
		super(layer, polygon, dataRow, true);
	}
	
	
	public VMapBuilding(VMapElementLayer layer, Vector2DH[][] polygon, Object[] dataRow) {
		super(layer, polygon, dataRow, true);
	}
	
	
	@Override
	protected void generateOutline(FlatRegion region, World w, TriangleList triangleList) {
		
		IBlockState state = VMapBlockSelector.getBlockState(this);
		if(state == null) return;
		
		int buildingHeight = VMapBlockSelector.getAdditionalHeight(this);
		
		LineGenerator lineGenerator = new LineGenerator(
				v -> (int) Math.round(triangleList.interpolateHeight(v)),
				w, region, state
		);
		
		int maxHeight = this.getMaxTerrainHeight(triangleList) + buildingHeight;
		
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
	protected void fillBlocks(FlatRegion region, World w, TriangleList triangleList) {
		
		IBlockState state = VMapBlockSelector.getBlockState(this);
		if(state == null) return;
		
		int buildingHeight = VMapBlockSelector.getAdditionalHeight(this);

		Vector region_vmin = region.getMinimumPoint(), region_vmax = region.getMaximumPoint();
		int region_xmin = (int) Math.floor(region_vmin.getX()), region_xmax = (int) Math.ceil(region_vmax.getX());
		int region_zmin = (int) Math.floor(region_vmin.getZ()), region_zmax = (int) Math.ceil(region_vmax.getZ());
		
		BoundingBox box = this.getBoundingBox();
		int vertex_xmin = (int) Math.floor(box.xmin), vertex_xmax = (int) Math.ceil(box.xmax);
		int vertex_zmin = (int) Math.floor(box.zmin), vertex_zmax = (int) Math.ceil(box.zmax);
		
		int xmin = Math.max(region_xmin, vertex_xmin), zmin = Math.max(region_zmin, vertex_zmin);
		int xmax = Math.min(region_xmax, vertex_xmax), zmax = Math.min(region_zmax, vertex_zmax);
		
		int y = this.getMaxTerrainHeight(triangleList) + buildingHeight - 1;
		
		for(int z = zmin; z <= zmax; ++z) {
			for(int x = xmin; x <= xmax; ++x) {
				if(!region.contains(new Vector(x, region.getMinimumY(), z)) || !this.containsPoint(new Vector2DH(x+.5, z+.5))) continue;
				w.setBlockState(new BlockPos(x, y, z), state);
			}
		}
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
