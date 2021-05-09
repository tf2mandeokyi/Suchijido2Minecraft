package com.mndk.kvm2m.mod.task;

import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vmap.elem.VMapElement;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.world.World;

public class VMapElemsGenTask implements VMapGeneratorTask {

	private final VMapElement[] elements;
	private final World world;
	private final FlatRegion worldEditRegion;
	private final TriangleList triangleList;
	private final int size;
	
	public VMapElemsGenTask(VMapElement[] elements, World world, FlatRegion worldEditRegion, TriangleList triangleList) {
		this.elements = elements;
		this.world = world;
		this.worldEditRegion = worldEditRegion;
		this.triangleList = triangleList;
		int size = 0;
		for(VMapElement element : elements) {
			if(element != null) ++size;
		}
		this.size = size;
	}
	
	@Override
	public void doTask() {
		for(VMapElement element : elements) {
			if(element != null) element.generateBlocks(this.worldEditRegion, this.world, this.triangleList);
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
