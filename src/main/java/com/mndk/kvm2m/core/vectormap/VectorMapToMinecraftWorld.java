package com.mndk.kvm2m.core.vectormap;

import com.mndk.kvm2m.core.util.delaunator.FastDelaunayTriangulator;
import com.mndk.kvm2m.core.util.shape.Triangle;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vectormap.elem.poly.VectorMapPolyline;
import com.mndk.kvm2m.mod.KVectorMap2MinecraftMod;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
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
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class VectorMapToMinecraftWorld {

	
	
	private static IBlockState CONTOUR_AREA_BLOCK = Blocks.EMERALD_BLOCK.getDefaultState();
	
	
	
    public static void generate(MinecraftServer server, EntityPlayerMP player, VectorMapParserResult result) throws VectorMapParserException {

        World world = server.getEntityWorld();
        
        // Validating world type
        validateWorld(world);
        
        // Validating world edit region
        FlatRegion worldEditRegion = validateWorldEditRegion(world, player);
        
        // Generate triangles based on contour lines with delaunay triangulate algorithm
    	TriangleList triangleList = FastDelaunayTriangulator.from(result.getElevationPoints()).getTriangleList();

        KVectorMap2MinecraftMod.logger.info("Generating surface...");
        
        for(Triangle triangle : triangleList) {
        	triangle.rasterize(world, worldEditRegion, CONTOUR_AREA_BLOCK);
        }
		
		KVectorMap2MinecraftMod.logger.info("Generating lines...");
		
		for(VectorMapPolyline polyline : result.getPolylines()) {
			polyline.generateBlocks(worldEditRegion, world, triangleList);
		}
        
        KVectorMap2MinecraftMod.logger.info("Successfully placed all blocks in the entry list.");
        
    }
    
    
    
    private static void validateWorld(World world) throws VectorMapParserException {
    	
    	IChunkProvider chunkProvider = world.getChunkProvider();
        if(!(chunkProvider instanceof CubeProviderServer)) {
            throw new VectorMapParserException("You must be in a cubic chunks world to generate .dxf map.");
        }

        ICubeGenerator cubeGenerator = ((CubeProviderServer) chunkProvider).getCubeGenerator();
        if (!(cubeGenerator instanceof EarthGenerator)) {
            throw new VectorMapParserException("You must be in a terra 1 to 1 world to generate .dxf map.");
        }
    	
    }
    
    
    
    private static FlatRegion validateWorldEditRegion(World world, EntityPlayerMP player) throws VectorMapParserException {
    	
    	ForgeWorld weWorld = ForgeWorldEdit.inst.getWorld(world);
    	LocalSession session = ForgeWorldEdit.inst.getSession(player);
    	Region worldEditRegion;
    	try {
    		worldEditRegion = session.getSelection(weWorld);
    		if(!(worldEditRegion instanceof FlatRegion)) {
    			throw new VectorMapParserException("Worldedit region should be either cuboid, cylinder, or polygon.");
    		}
    	} catch(IncompleteRegionException exception) {
    		// No region is selected
    		throw new VectorMapParserException("Please select the worldedit region first.");
    	}
    	return (FlatRegion) worldEditRegion;
    	
    }

}
