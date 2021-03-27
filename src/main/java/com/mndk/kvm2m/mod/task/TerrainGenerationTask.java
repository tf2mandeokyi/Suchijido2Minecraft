package com.mndk.kvm2m.mod.task;

import com.mndk.kvm2m.core.util.shape.Triangle;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class TerrainGenerationTask implements VMapGeneratorTask {

	private final TriangleList triangleList;
	private final World world;
	private final FlatRegion worldEditRegion;
	
	public TerrainGenerationTask(TriangleList triangleList, World world, FlatRegion worldEditRegion) {
		this.triangleList = triangleList;
		this.world = world;
		this.worldEditRegion = worldEditRegion;
	}
	
	@Override
	public void doTask() {
		for(Triangle triangle : this.triangleList) {
        	triangle.rasterize(this.world, this.worldEditRegion, Blocks.GRASS.getDefaultState());
        }
	}

	@Override
	public String getBroadcastMessage() {
		return "§dGenerating surface...";
	}

}
