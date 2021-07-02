package com.mndk.kvm2m.mod.task;

import com.mndk.kvm2m.core.util.shape.Triangle;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.world.World;

public class TerrainFillingTask implements VMapGeneratorTask {

	private final TriangleList triangleList;
	private final World world;
	private final FlatRegion worldEditRegion;
	
	public TerrainFillingTask(TriangleList triangleList, World world, FlatRegion worldEditRegion) {
		this.triangleList = triangleList;
		this.world = world;
		this.worldEditRegion = worldEditRegion;
	}
	
	@Override
	public void run() {
		for(Triangle triangle : this.triangleList) {
			triangle.fillBlocksBelow(this.world, this.worldEditRegion);
		}
	}

	@Override
	public String getBroadcastMessage() {
		return "§dFilling terrain...";
	}

	@Override
	public int getSize() {
		return triangleList.size();
	}

}
