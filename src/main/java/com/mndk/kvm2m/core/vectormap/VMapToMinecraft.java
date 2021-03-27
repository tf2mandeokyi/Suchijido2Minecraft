package com.mndk.kvm2m.core.vectormap;

import java.util.ArrayList;
import java.util.List;

import com.mndk.kvm2m.core.util.delaunator.FastDelaunayTriangulator;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vectormap.elem.VMapElement;
import com.mndk.kvm2m.mod.event.ServerTickRepeater;
import com.mndk.kvm2m.mod.task.TerrainCuttingTask;
import com.mndk.kvm2m.mod.task.TerrainGenerationTask;
import com.mndk.kvm2m.mod.task.VMapObjGenTask;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.world.World;

public class VMapToMinecraft {


	static final int MAX_AXIS = 30000000;
	static final FlatRegion INFINITE_REGION = new CuboidRegion(new Vector(-MAX_AXIS, 0, -MAX_AXIS), new Vector(MAX_AXIS, 0, MAX_AXIS));
	
	
    public static void generateTasks(World world, FlatRegion worldEditRegion, VMapParserResult result) throws VMapParserException {
        
    	if(worldEditRegion == null) worldEditRegion = INFINITE_REGION;
    	
        // Schedule triangles generation task based on contour lines with delaunay triangulate algorithm
    	TriangleList triangleList = FastDelaunayTriangulator.from(result.getElevationPoints()).getTriangleList();
    	ServerTickRepeater.addTask(new TerrainGenerationTask(triangleList, world, worldEditRegion));
    	ServerTickRepeater.addTask(new TerrainCuttingTask(triangleList, world, worldEditRegion));
		
        List<VMapElement> elementList = new ArrayList<>(), totalElements = result.getElements();
        VMapObjectType lastType;
        VMapElement temp;
        
	    if(!totalElements.isEmpty()) {
	        elementList.add(temp = totalElements.get(0));
	        lastType = temp.getType();
			for(int i = 1; i < totalElements.size(); ++i) {
				temp = totalElements.get(i);
				if(lastType != temp.getType()) {
					ServerTickRepeater.addTask(new VMapObjGenTask(elementList, lastType, world, worldEditRegion, triangleList));
					lastType = temp.getType();
					elementList = new ArrayList<>();
				}
				else {
					elementList.add(temp);
				}
			}
			ServerTickRepeater.addTask(new VMapObjGenTask(elementList, lastType, world, worldEditRegion, triangleList));
        }
	    // dirty code lmao
	    // TODO categorize map objects by layers
        
    }

}
