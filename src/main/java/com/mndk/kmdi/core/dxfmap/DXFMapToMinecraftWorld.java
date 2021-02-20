package com.mndk.kmdi.core.dxfmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.kabeja.parser.ParseException;

import com.mndk.kmdi.core.dxfmap.elem.polyline.DXFMapContour;
import com.mndk.kmdi.core.dxfmap.elem.polyline.DXFMapPolyline;
import com.mndk.kmdi.core.math.ContourMath;
import com.mndk.kmdi.core.math.shape.BoundingBox;
import com.mndk.kmdi.core.we.LineGenerator;
import com.mndk.kmdi.mod.KmdiMod;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.forge.ForgeWorld;
import com.sk89q.worldedit.forge.ForgeWorldEdit;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;

import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import net.buildtheearth.terraplusplus.generator.EarthGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class DXFMapToMinecraftWorld {

	private static IBlockState CONTOUR_BLOCK = Blocks.GOLD_BLOCK.getDefaultState();
	private static IBlockState CONTOUR_AREA_BLOCK = Blocks.EMERALD_BLOCK.getDefaultState();
	
    public static void generate(MinecraftServer server, EntityPlayerMP player, File mapFile) throws GeneratorException {

        World world = server.getEntityWorld();
        IChunkProvider chunkProvider = world.getChunkProvider();
        if(!(chunkProvider instanceof CubeProviderServer)) {
            throw new GeneratorException("You must be in cc map to generate .dxf map.");
        }

        ICubeGenerator cubeGenerator = ((CubeProviderServer) chunkProvider).getCubeGenerator();
        if (!(cubeGenerator instanceof EarthGenerator)) {
            throw new GeneratorException("You must be in terra map to generate .dxf map.");
        }
        
    	ForgeWorld weWorld = ForgeWorldEdit.inst.getWorld(world);
    	LocalSession session = ForgeWorldEdit.inst.getSession(player);
    	Region worldEditRegion;
    	try {
    		worldEditRegion = session.getSelection(weWorld);
    		if(!(worldEditRegion instanceof CuboidRegion) && !(worldEditRegion instanceof Polygonal2DRegion)) {
    			throw new GeneratorException("Worldedit region should be either cuboid or polygon.");
    		}
    	} catch(IncompleteRegionException exception) {
    		// No region is selected
    		throw new GeneratorException("Please select the worldedit region first.");
    	}

        DXFMapParser.Result result;
        try {
            result = DXFMapParser.parse(mapFile);
        } catch(ParseException exception) {
            throw new GeneratorException("There was an error while generating .dxf map.");
        } catch(FileNotFoundException exception) {
        	throw new GeneratorException("File not found!");
        }
        
        KmdiMod.logger.info("Successfully parsed .dxf map: " + mapFile.getName() + " (Next: generating blocks");
        generateBlocksBasedOnDXFMapParsedResult(result, worldEditRegion, world);
        
        KmdiMod.logger.info("Successfully placed all blocks in the entry list.");
        
    }
    
    public static void generateBlocksBasedOnDXFMapParsedResult(DXFMapParser.Result result, Region worldEditRegion, World world) {
    	
    	List<DXFMapContour> contourList = result.getContourList();
    	DXFMapPolyline boundary = result.getBoundary();

    	Vector2D min = worldEditRegion.getMinimumPoint().toVector2D();
    	Vector2D max = worldEditRegion.getMaximumPoint().toVector2D();
    	
		int x_0 = (int) Math.floor(min.getX()), x_max = (int) Math.ceil(max.getX());
        int z_0 = (int) Math.floor(min.getZ()), z_max = (int) Math.ceil(max.getZ());

        KmdiMod.logger.info("Generating surface...");
    	
        Vector2D point;
		
        if(worldEditRegion instanceof CuboidRegion) {
        	for(int z = z_0; z <= z_max; z++) {
    			for(int x = x_0; x <= x_max; x++) {
    				point = new Vector2D(x, z);
    				
    				if(!boundary.containsPoint(point)) continue;
    				
    				double height = ContourMath.getPointHeightFromContourList(point, contourList);
    				if(height != height) continue;
    				
    				Vector vector = point.toVector(height);
    				BlockPos pos = new BlockPos(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    				
    				world.setBlockState(pos, CONTOUR_AREA_BLOCK);
    			}
    		}
        }
        else if(worldEditRegion instanceof Polygonal2DRegion) {
        	DXFMapPolyline polySelection = new DXFMapPolyline((Polygonal2DRegion) worldEditRegion);
        	for(int z = z_0; z <= z_max; z++) {
    			for(int x = x_0; x <= x_max; x++) {
    				point = new Vector2D(x, z);
    				
    				if(!boundary.containsPoint(point) || !polySelection.containsPoint(point)) {
    					continue;
    				}
    					
    				double height = ContourMath.getPointHeightFromContourList(point, contourList);
    				if(height != height) continue;
    				
    				Vector vector = point.toVector(height);
    				BlockPos pos = new BlockPos(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());

    				world.setBlockState(pos, CONTOUR_AREA_BLOCK);
    			}
    		}
        }
			
		KmdiMod.logger.info("Generating contour lines...");
		if(worldEditRegion instanceof CuboidRegion) {
			BoundingBox box = new BoundingBox((CuboidRegion) worldEditRegion);
			KmdiMod.logger.info("Cuboid bounding box, " + result.getContourList().size() + " contours total");
			
	        for(DXFMapContour contour : result.getContourList()) {
	        	for(int i=0;i<contour.getVertexCount()-1;i++) {
	        		if(!box.checkLineIntersection(contour.getVertex(i), contour.getVertex(i+1).subtract(contour.getVertex(i)))) {
	        			continue;
	        		}
	                LineGenerator.generateFlatLine(contour.getVertex(i), contour.getVertex(i+1), v -> {
	                	if(boundary.containsPoint(v) && box.containsPoint(v)) {
		    				BlockPos pos = new BlockPos(v.getBlockX(), contour.getElevation(), v.getBlockZ());
		    				world.setBlockState(pos, CONTOUR_BLOCK);
	                	}
	                });
	            }
	        }
		}
		else if(worldEditRegion instanceof Polygonal2DRegion) {
        	DXFMapPolyline polySelection = new DXFMapPolyline((Polygonal2DRegion) worldEditRegion);
			KmdiMod.logger.info("Polygonal bounding box, " + result.getContourList() + " contours total");
        	
			for(DXFMapContour contour : result.getContourList()) {
	        	for(int i=0;i<contour.getVertexCount()-1;i++) {
	        		if(!polySelection.checkLineIntersection(contour.getVertex(i), contour.getVertex(i+1).subtract(contour.getVertex(i)))) {
	        			continue;
	        		}
	                LineGenerator.generateFlatLine(contour.getVertex(i), contour.getVertex(i+1), v -> {
	                	if(boundary.containsPoint(v) && polySelection.containsPoint(v)) {
		    				BlockPos pos = new BlockPos(v.getBlockX(), contour.getElevation(), v.getBlockZ());
		    				world.setBlockState(pos, CONTOUR_BLOCK);
	                	}
	                });
	            }
	        }
		}
        
    }

    @SuppressWarnings("serial")
	public static class GeneratorException extends Exception {
        public GeneratorException(String message) {
            super(message);
        }
    }

}
