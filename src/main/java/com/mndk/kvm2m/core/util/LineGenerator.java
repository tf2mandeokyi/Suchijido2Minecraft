package com.mndk.kvm2m.core.util;

import java.util.function.Function;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.Region;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LineGenerator {

	public static int y;
	public static Function<Vector2DH, Integer> getYFunction;
	public static World world;
	public static Region region;
	public static IBlockState state;

    
    
    /**
	 * Initialize static members {@link getYFunction}, {@link containsFunction}, {@link world}, {@link region}, and {@link state} first before calling this method.
	 * */
    public static void generateLine(Vector2DH v1, Vector2DH v2) {

        double dx = v2.x - v1.x, dz = v2.z - v1.z;
        double dxa = Math.abs(dx), dza = Math.abs(dz);
        double xs, xe, zs, ze;

        if(dx == 0 && dz == 0) {
        	placeBlock(v1.x, v1.z);
            return;
        }

        if(dx > 0) { xs = v1.x; xe = v2.x; }
        else /*dx < 0*/ { xs = v2.x; xe = v1.x; }
        if(dz > 0) { zs = v1.z; ze = v2.z; }
        else /*dz < 0*/ { zs = v2.z; ze = v1.z; }
        
        if(dxa == 0) {
        	double x = v1.x;
            for(double z = zs; z < ze; z++) placeBlock(x, z);
            placeBlock(x, ze);
        } else if(dza == 0) {
        	double z = v1.z;
            for(double x = xs; x < xe; x++) placeBlock(x, z);
            placeBlock(xe, z);
        } else if(dxa > dza) {
            for(double x = xs; x < xe; x++) placeBlock(x, v1.z + dz * (x - v1.x) / dx);
            placeBlock(xe, v1.z + dz * (xe - v1.x) / dx);
        } else {
            for(double z = zs; z < ze; z++) placeBlock(v1.x + dx * (z - v1.z) / dz, z);
        	placeBlock(v1.x + dx * (ze - v1.z) / dz, ze);
        }
    }
    
    
    
    private static void placeBlock(double x, double z) {
    	int y = getYFunction.apply(new Vector2DH(x, z));
    	if(!region.contains(new Vector(Math.floor(x), Math.floor(y), Math.floor(z)))) return;
    	world.setBlockState(new BlockPos(x, y, z), state);
    }

}
