package com.mndk.kvm2m.mod.task;

import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vmap.elem.VMapElement;
import com.mndk.kvm2m.core.vmap.elem.VMapLayer;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.world.World;

public class VMapElemLayerGenTask implements VMapGeneratorTask {

	private final VMapLayer elementLayer;
	private final World world;
	private final FlatRegion worldEditRegion;
	private final TriangleList triangleList;
	
	public VMapElemLayerGenTask(VMapLayer elementLayer, World world, FlatRegion worldEditRegion, TriangleList triangleList) {
		this.elementLayer = elementLayer;
		this.world = world;
		this.worldEditRegion = worldEditRegion;
		this.triangleList = triangleList;
	}
	
	@Override
	public void doTask() {
		for(VMapElement element : this.elementLayer) {
			element.generateBlocks(this.worldEditRegion, this.world, this.triangleList);
		}
	}

	@Override
	public String getBroadcastMessage() {
		return "§dGenerating object layer \"" + elementLayer.getType() + "\" (" + elementLayer.size() + ")...";
	}

	@Override
	public int getSize() {
		return elementLayer.size();
	}

}
