package com.mndk.kmdi.core.util;

import java.util.function.Function;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.Region;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LineGenerator {

	public static int y;
	public static Function<Vector2D, Integer> getYFunction;
	public static World world;
	public static Region region;
	public static IBlockState state;
	
	/**
	 * Initialize static members {@link y}, {@link world}, {@link region}, and {@link state} first before calling this method.
	 * */
    public static void generateFlatLine(Vector2D v1, Vector2D v2) {

        double dx = v2.getX() - v1.getX(), dz = v2.getZ() - v1.getZ();
        double dxa = Math.abs(dx), dza = Math.abs(dz);
        double xs, xe, zs, ze, x, z;

        if(dx == 0 && dz == 0) {
        	x = v1.getBlockX(); z = v1.getBlockZ();
        	if(region.contains(new Vector(x, y, z))) world.setBlockState(new BlockPos(x, y, z), state);
            return;
        }

        if(dx > 0) { xs = v1.getX(); xe = v2.getX(); }
        else /*dx < 0*/ { xs = v2.getX(); xe = v1.getX(); }
        if(dz > 0) { zs = v1.getZ(); ze = v2.getZ(); }
        else /*dz < 0*/ { zs = v2.getZ(); ze = v1.getZ(); }
        
        if(dxa == 0) {
        	x = v1.getX();
            for(z = zs; z <= ze; z++) {
            	if(region.contains(new Vector(x, y, z))) world.setBlockState(new BlockPos(x, y, z), state);
            }
        } else if(dza == 0) {
        	z = v1.getZ();
            for(x = xs; x <= xe; x++) {
            	if(region.contains(new Vector(x, y, z))) world.setBlockState(new BlockPos(x, y, z), state);
            }
        } else if(dxa > dza) {
            for(x = xs; x <= xe; x++) {
            	z = v1.getZ() + dz * (x - v1.getX()) / dx;
            	if(region.contains(new Vector(x, y, z))) world.setBlockState(new BlockPos(x, y, z), state);
            }
        } else {
            for(z = zs; z <= ze; z++) {
                x = v1.getX() + dx * (z - v1.getZ()) / dz;
                if(region.contains(new Vector(x, y, z))) world.setBlockState(new BlockPos(x, y, z), state);
            }
        }
    }

    
    
    /**
	 * Initialize static members {@link getYFunction}, {@link world}, {@link region}, and {@link state} first before calling this method.
	 * */
    public static void generateLineByFunction(Vector2D v1, Vector2D v2) {

        double dx = v2.getX() - v1.getX(), dz = v2.getZ() - v1.getZ();
        double dxa = Math.abs(dx), dza = Math.abs(dz);
        double xs, xe, zs, ze, x, z;
        int y;

        if(dx == 0 && dz == 0) {
        	x = v1.getBlockX(); z = v1.getBlockZ();
        	y = getYFunction.apply(v1);
        	if(region.contains(new Vector(x, y, z))) world.setBlockState(new BlockPos(x, y, z), state);
            return;
        }

        if(dx > 0) { xs = v1.getX(); xe = v2.getX(); }
        else /*dx < 0*/ { xs = v2.getX(); xe = v1.getX(); }
        if(dz > 0) { zs = v1.getZ(); ze = v2.getZ(); }
        else /*dz < 0*/ { zs = v2.getZ(); ze = v1.getZ(); }
        
        if(dxa == 0) {
        	x = v1.getX();
            for(z = zs; z <= ze; z++) {
            	y = getYFunction.apply(new Vector2D(x, z));
            	if(region.contains(new Vector(x, y, z))) world.setBlockState(new BlockPos(x, y, z), state);
            }
        } else if(dza == 0) {
        	z = v1.getZ();
            for(x = xs; x <= xe; x++) {
            	y = getYFunction.apply(new Vector2D(x, z));
            	if(region.contains(new Vector(x, y, z))) world.setBlockState(new BlockPos(x, y, z), state);
            }
        } else if(dxa > dza) {
            for(x = xs; x <= xe; x++) {
            	z = v1.getZ() + dz * (x - v1.getX()) / dx;
            	y = getYFunction.apply(new Vector2D(x, z));
            	if(region.contains(new Vector(x, y, z))) world.setBlockState(new BlockPos(x, y, z), state);
            }
        } else {
            for(z = zs; z <= ze; z++) {
                x = v1.getX() + dx * (z - v1.getZ()) / dz;
            	y = getYFunction.apply(new Vector2D(x, z));
            	if(region.contains(new Vector(x, y, z))) world.setBlockState(new BlockPos(x, y, z), state);
            }
        }
    }

}
