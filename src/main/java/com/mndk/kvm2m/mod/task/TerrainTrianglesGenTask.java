package com.mndk.kvm2m.mod.task;

import com.mndk.kvm2m.core.util.shape.Triangle;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class TerrainTrianglesGenTask implements VMapGeneratorTask {
	
	private final Triangle[] triangles;
	private final World world;
	private final FlatRegion worldEditRegion;
	private final boolean cutTerrain, fillTerrain;
	private final int size;
	
	public TerrainTrianglesGenTask(Triangle[] triangles, World world, FlatRegion worldEditRegion, boolean cutTerrain, boolean fillTerrain) {
		this.triangles = triangles;
		this.world = world;
		this.worldEditRegion = worldEditRegion;
		this.cutTerrain = cutTerrain;
		this.fillTerrain = fillTerrain;
		int size = 0;
		for(Triangle triangle : triangles) {
			if(triangle != null) ++size;
		}
		this.size = size;
	}
	
	@Override
	public void doTask() {
		for(Triangle triangle : triangles) {
			if(triangle == null) continue;
			triangle.rasterize(world, worldEditRegion, Blocks.GRASS.getDefaultState());
			if(cutTerrain) triangle.removeTerrainAbove(world, worldEditRegion);
			if(fillTerrain) triangle.fillBlocksBelow(world, worldEditRegion);
		}
		
	}

	@Override
	public String getBroadcastMessage() {
		return null;
	}

	@Override
	public int getSize() {
		return size;
	}
	
	
}
