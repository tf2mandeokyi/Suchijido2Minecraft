package com.mndk.kvm2m.core.vectormap;

import com.mndk.kvm2m.core.util.delaunator.FastDelaunayTriangulator;
import com.mndk.kvm2m.core.util.shape.Triangle;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vectormap.elem.VectorMapElement;
import com.mndk.kvm2m.mod.KVectorMap2MinecraftMod;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.forge.ForgeWorld;
import com.sk89q.worldedit.forge.ForgeWorldEdit;
import com.sk89q.worldedit.regions.FlatRegion;
import com.sk89q.worldedit.regions.Region;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class VectorMapToMinecraftWorld {

	
	
	private static IBlockState CONTOUR_AREA_BLOCK = Blocks.EMERALD_BLOCK.getDefaultState();
	
	
	
    public static void generate(World world, EntityPlayerMP player, VectorMapParserResult result) throws VectorMapParserException {
        
        // Validating world edit region
        FlatRegion worldEditRegion = validateWorldEditRegion(world, player);
        
        // Generate triangles based on contour lines with delaunay triangulate algorithm
    	TriangleList triangleList = FastDelaunayTriangulator.from(result.getElevationPoints()).getTriangleList();

        KVectorMap2MinecraftMod.logger.info("Generating surface...");
        player.sendMessage(new TextComponentString("§dGenerating surface..."));
        
        for(Triangle triangle : triangleList) {
        	triangle.rasterize(world, worldEditRegion, CONTOUR_AREA_BLOCK);
        }
		
		KVectorMap2MinecraftMod.logger.info("Generating vector map elements...");
        player.sendMessage(new TextComponentString("§dGenerating vector map elements..."));
		
		for(VectorMapElement element : result.getElements()) {
			element.generateBlocks(worldEditRegion, world, triangleList);
		}
        
        KVectorMap2MinecraftMod.logger.info("Done!");
        player.sendMessage(new TextComponentString("§dDone!"));
        
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
