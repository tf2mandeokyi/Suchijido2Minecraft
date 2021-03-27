package com.mndk.kvm2m.mod.task;

import java.util.List;

import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vectormap.VectorMapObjectType;
import com.mndk.kvm2m.core.vectormap.elem.VectorMapElement;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.world.World;

public class MapObjectGenerationTask implements MapGeneratorTask {

	private final List<VectorMapElement> elementList;
	private final World world;
	private final FlatRegion worldEditRegion;
	private final TriangleList triangleList;
	private final VectorMapObjectType type;
	
	public MapObjectGenerationTask(List<VectorMapElement> elementList, VectorMapObjectType type, World world, FlatRegion worldEditRegion, TriangleList triangleList) {
		this.elementList = elementList;
		this.world = world;
		this.worldEditRegion = worldEditRegion;
		this.triangleList = triangleList;
		this.type = type;
	}
	
	@Override
	public void doTask() {
		for(VectorMapElement element : this.elementList) {
			element.generateBlocks(this.worldEditRegion, this.world, this.triangleList);
		}
	}

	@Override
	public String getBroadcastMessage() {
		return "§dGenerating object layer (" + type + ", object count = " + elementList.size() + ")...";
	}

}
