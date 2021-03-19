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
        double xs, xe, zs, ze, x, z;
        int y;

        if(dx == 0 && dz == 0) {
        	x = v1.x; z = v1.z;
        	y = getYFunction.apply(v1);
        	if(region.contains(new Vector(x, y, z))) world.setBlockState(new BlockPos(x, y, z), state);
            return;
        }

        if(dx > 0) { xs = v1.x; xe = v2.x; }
        else /*dx < 0*/ { xs = v2.x; xe = v1.x; }
        if(dz > 0) { zs = v1.z; ze = v2.z; }
        else /*dz < 0*/ { zs = v2.z; ze = v1.z; }
        
        if(dxa == 0) {
        	x = v1.x;
            for(z = zs; z <= ze; z++) {
            	y = getYFunction.apply(new Vector2DH(x, z));
            	if(!region.contains(new Vector(x, y, z))) continue;
            	world.setBlockState(new BlockPos(x, y, z), state);
            }
        } else if(dza == 0) {
        	z = v1.z;
            for(x = xs; x <= xe; x++) {
            	y = getYFunction.apply(new Vector2DH(x, z));
            	if(!region.contains(new Vector(x, y, z))) continue;
            	world.setBlockState(new BlockPos(x, y, z), state);
            }
        } else if(dxa > dza) {
            for(x = xs; x <= xe; x++) {
            	z = v1.z + dz * (x - v1.x) / dx;
            	y = getYFunction.apply(new Vector2DH(x, z));
            	if(!region.contains(new Vector(x, y, z))) continue;
            	world.setBlockState(new BlockPos(x, y, z), state);
            }
        } else {
            for(z = zs; z <= ze; z++) {
                x = v1.x + dx * (z - v1.z) / dz;
            	y = getYFunction.apply(new Vector2DH(x, z));
            	if(!region.contains(new Vector(x, y, z))) continue;
            	world.setBlockState(new BlockPos(x, y, z), state);
            }
        }
    }

}
