package com.mndk.kvm2m.mod.task;

import com.mndk.kvm2m.core.util.shape.Triangle;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.world.World;

public class TerrainCuttingTask implements VMapGeneratorTask {

	private final TriangleList triangleList;
	private final World world;
	private final FlatRegion worldEditRegion;
	
	public TerrainCuttingTask(TriangleList triangleList, World world, FlatRegion worldEditRegion) {
		this.triangleList = triangleList;
		this.world = world;
		this.worldEditRegion = worldEditRegion;
	}
	
	@Override
	public void doTask() {
		for(Triangle triangle : this.triangleList) {
        	triangle.removeTerrainAbove(this.world, this.worldEditRegion);
        }
	}

	@Override
	public String getBroadcastMessage() {
		return "§dCutting surface...";
	}

}
