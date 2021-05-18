package com.mndk.kvm2m.core.vmap;

import java.util.List;
import java.util.Map;

import com.mndk.kvm2m.core.util.shape.Triangle;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.util.triangulator.TerrainTriangulator;
import com.mndk.kvm2m.core.vmap.elem.VMapElement;
import com.mndk.kvm2m.core.vmap.elem.VMapElementLayer;
import com.mndk.kvm2m.mod.event.ServerTickRepeater;
import com.mndk.kvm2m.mod.task.TerrainCuttingTask;
import com.mndk.kvm2m.mod.task.TerrainFillingTask;
import com.mndk.kvm2m.mod.task.TerrainGenerationTask;
import com.mndk.kvm2m.mod.task.TerrainTrianglesGenTask;
import com.mndk.kvm2m.mod.task.VMapElemLayerGenTask;
import com.mndk.kvm2m.mod.task.VMapElemsGenTask;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.world.World;

public class VMapToMinecraft {
	
	
	public static void generateTasks(World world, FlatRegion worldEditRegion, VMapParserResult result, Map<String, String> options) throws VMapParserException {
		
		String elementPerTickStr = options.get("element-per-tick");
		
		if(elementPerTickStr != null) {
			int elementsPerTick = assertInteger(elementPerTickStr, "The value of \"element-per-tick\" should be an integer!");
			
			generateTasksPerTick(elementsPerTick, world, worldEditRegion, result, options);
		}
		else {
			List<VMapElementLayer> layerList = result.getElementLayers();
			
			TriangleList triangleList = TerrainTriangulator.generate(result);
			
			// Schedule triangles generation task based on contour lines with delaunay triangulate algorithm
			if(!options.containsKey("no-terrain")) {
				ServerTickRepeater.addTask(new TerrainGenerationTask(triangleList, world, worldEditRegion));
				if(!options.containsKey("no-cutting")) {
					ServerTickRepeater.addTask(new TerrainCuttingTask(triangleList, world, worldEditRegion));
				}
				if(!options.containsKey("no-filling")) {
					ServerTickRepeater.addTask(new TerrainFillingTask(triangleList, world, worldEditRegion));
				}
			}
			
			if(!options.containsKey("terrain-only")) {
				if(!layerList.isEmpty()) {
					for(VMapElementLayer elementLayer : layerList) {
						
						VMapElementType type = elementLayer.getType();
						
						if(options.containsKey("layer-only")) {
							if(!type.equals(VMapElementType.fromLayerName(options.get("layer-only")))) {
								continue;
							}
						}
						if(!options.containsKey("draw-contour")) {
							if(type == VMapElementType.등고선 || type == VMapElementType.표고점) {
								continue;
							}
						}
						
						ServerTickRepeater.addTask(new VMapElemLayerGenTask(elementLayer, world, worldEditRegion, triangleList));
					}
				}
			}
		}
		
	}
	
	
	
	private static void generateTasksPerTick(
			int elementsPerTick,
			World world,
			FlatRegion worldEditRegion,
			VMapParserResult result,
			Map<String, String> options
	) throws VMapParserException {
		
		if(elementsPerTick <= 0) throw new VMapParserException("The value of \"element-per-tick\" should be bigger than 0!");
		
		Triangle[] triangleArray = new Triangle[elementsPerTick];
		int i = 0;

		List<VMapElementLayer> layerList;
		TriangleList triangleList = TerrainTriangulator.generate(result);
		boolean cutTerrain = !options.containsKey("no-cutting"), fillTerrain = !options.containsKey("no-filling");
		
		if(!options.containsKey("no-terrain")) {
			for(Triangle triangle : triangleList) {
				if(triangle == null) continue;
				triangleArray[i++] = triangle;
				if(i == elementsPerTick) {
					ServerTickRepeater.addTask(new TerrainTrianglesGenTask(triangleArray, world, worldEditRegion, cutTerrain, fillTerrain));
					i = 0;
					triangleArray = new Triangle[elementsPerTick];
				}
			}
			ServerTickRepeater.addTask(new TerrainTrianglesGenTask(triangleArray, world, worldEditRegion, cutTerrain, fillTerrain));
		}
		
		i = 0;
		if(!options.containsKey("terrain-only")) {
			layerList = result.getElementLayers();
			VMapElement[] mapElementArray = new VMapElement[elementsPerTick];
			
			for(VMapElementLayer elementLayer : layerList) {
				
				VMapElementType type = elementLayer.getType();
				
				if(options.containsKey("layer-only")) {
					if(!type.equals(VMapElementType.fromLayerName(options.get("layer-only")))) {
						continue;
					}
				}
				if(!options.containsKey("draw-contour")) {
					if(type == VMapElementType.등고선 || type == VMapElementType.표고점) {
						continue;
					}
				}
				for(VMapElement element : elementLayer) {
					if(element == null) continue;
					mapElementArray[i++] = element;
					if(i == elementsPerTick) {
						ServerTickRepeater.addTask(new VMapElemsGenTask(mapElementArray, world, worldEditRegion, triangleList));
						i = 0;
						mapElementArray = new VMapElement[elementsPerTick];
					}
				}
			}
			ServerTickRepeater.addTask(new VMapElemsGenTask(mapElementArray, world, worldEditRegion, triangleList));
			i = 0;
			mapElementArray = new VMapElement[elementsPerTick];
		}
	}
	
	
	
	private static int assertInteger(String value, String errorMessage) throws VMapParserException {
		try { return Integer.parseInt(value); } 
		catch(NumberFormatException e) { throw new VMapParserException(errorMessage); }
	}

}
