package com.mndk.kvm2m.core.dxfmap;

import java.io.File;
import java.io.FileNotFoundException;

import org.kabeja.parser.ParseException;

import com.mndk.kvm2m.core.dxfmap.elem.polyline.DXFMapPolyline;
import com.mndk.kvm2m.core.util.delaunator.FastDelaunayTriangulator;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.mod.KVectorMap2MinecraftMod;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.forge.ForgeWorld;
import com.sk89q.worldedit.forge.ForgeWorldEdit;
import com.sk89q.worldedit.regions.FlatRegion;
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

	
	
	private static IBlockState CONTOUR_AREA_BLOCK = Blocks.EMERALD_BLOCK.getDefaultState();
	
	
	
    public static void generate(MinecraftServer server, EntityPlayerMP player, File mapFile) throws GeneratorException {

        World world = server.getEntityWorld();
        
        // Validating world type
        validateWorld(world);
        
        // Validating world edit region
        FlatRegion worldEditRegion = validateWorldEditRegion(world, player);
        
        // Validating imported .dxf map
        DXFMapParser.Result result = validateDxfMap(mapFile);
    	DXFMapPolyline boundary = result.getBoundary();
        
        KVectorMap2MinecraftMod.logger.info("Successfully parsed .dxf map: " + mapFile.getName());
        
        // Generate triangles with delaunay triangulate algorithm
    	TriangleList triangleList = FastDelaunayTriangulator.from(result.getElevationPoints()).getTriangleList();

        KVectorMap2MinecraftMod.logger.info("Generating surface...");
    	
        Iterable<Vector2D> vector2ds = worldEditRegion.asFlatRegion();
        for(Vector2D v : vector2ds) {
        	Vector2DH temp = new Vector2DH(v);
			
			if(!worldEditRegion.contains(temp.toWorldEditVector()) || !boundary.containsPoint(temp)) continue;

			double height = triangleList.interpolateHeight(temp);
			if(height != height) continue;
			
			BlockPos pos = new BlockPos((int) temp.x, Math.round(height), (int) temp.z);

			world.setBlockState(pos, CONTOUR_AREA_BLOCK);
        }
		
		KVectorMap2MinecraftMod.logger.info("Generating lines...");
		
		for(DXFMapPolyline polyline : result.getPolylines()) {
			polyline.generatePolygonOnTerrain(worldEditRegion, world, polyline.getType().getBlockState(), triangleList);
		}
        
        KVectorMap2MinecraftMod.logger.info("Successfully placed all blocks in the entry list.");
        
    }
    
    
    
    private static void validateWorld(World world) throws GeneratorException {
    	
    	IChunkProvider chunkProvider = world.getChunkProvider();
        if(!(chunkProvider instanceof CubeProviderServer)) {
            throw new GeneratorException("You must be in cc map to generate .dxf map.");
        }

        ICubeGenerator cubeGenerator = ((CubeProviderServer) chunkProvider).getCubeGenerator();
        if (!(cubeGenerator instanceof EarthGenerator)) {
            throw new GeneratorException("You must be in terra map to generate .dxf map.");
        }
    	
    }
    
    
    
    private static FlatRegion validateWorldEditRegion(World world, EntityPlayerMP player) throws GeneratorException {
    	
    	ForgeWorld weWorld = ForgeWorldEdit.inst.getWorld(world);
    	LocalSession session = ForgeWorldEdit.inst.getSession(player);
    	Region worldEditRegion;
    	try {
    		worldEditRegion = session.getSelection(weWorld);
    		if(!(worldEditRegion instanceof FlatRegion)) {
    			throw new GeneratorException("Worldedit region should be either cuboid, cylinder, or polygon.");
    		}
    	} catch(IncompleteRegionException exception) {
    		// No region is selected
    		throw new GeneratorException("Please select the worldedit region first.");
    	}
    	return (FlatRegion) worldEditRegion;
    	
    }
    
    
    
    private static DXFMapParser.Result validateDxfMap(File file) throws GeneratorException {
    	try {
            return DXFMapParser.parse(file);
        } catch(ParseException exception) {
            throw new GeneratorException("There was an error while parsing .dxf map.");
        } catch(FileNotFoundException exception) {
        	throw new GeneratorException("File not found!");
        }
    }
    
    

    @SuppressWarnings("serial")
	public static class GeneratorException extends Exception {
        public GeneratorException(String message) {
            super(message);
        }
    }

}
