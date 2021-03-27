package com.mndk.kvm2m.mod.task;

import java.util.List;

import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vectormap.VMapObjectType;
import com.mndk.kvm2m.core.vectormap.elem.VMapElement;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.world.World;

public class VMapObjGenTask implements VMapGeneratorTask {

	private final List<VMapElement> elementList;
	private final World world;
	private final FlatRegion worldEditRegion;
	private final TriangleList triangleList;
	private final VMapObjectType type;
	
	public VMapObjGenTask(List<VMapElement> elementList, VMapObjectType type, World world, FlatRegion worldEditRegion, TriangleList triangleList) {
		this.elementList = elementList;
		this.world = world;
		this.worldEditRegion = worldEditRegion;
		this.triangleList = triangleList;
		this.type = type;
	}
	
	@Override
	public void doTask() {
		for(VMapElement element : this.elementList) {
			element.generateBlocks(this.worldEditRegion, this.world, this.triangleList);
		}
	}

	@Override
	public String getBroadcastMessage() {
		return "§dGenerating object layer (" + type + ", object count = " + elementList.size() + ")...";
	}

}
