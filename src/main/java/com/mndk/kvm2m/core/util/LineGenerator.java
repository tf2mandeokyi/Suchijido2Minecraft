package com.mndk.kvm2m.core.util;

import java.util.function.Function;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LineGenerator {

	public final Function<Vector2DH, Integer> getYFunction;
	public final World world;
	public final FlatRegion region;
	public final IBlockState state;

	
	
    public LineGenerator(Function<Vector2DH, Integer> getYFunction, World world, FlatRegion region, IBlockState state) {
		this.getYFunction = getYFunction;
		this.world = world;
		this.region = region;
		this.state = state;
	}

    

    public void generateLine(Vector2DH v1, Vector2DH v2) {

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

    
    
    public void generateLineWithMaxHeight(Vector2DH v1, Vector2DH v2, int maxHeight) {

        double dx = v2.x - v1.x, dz = v2.z - v1.z;
        double dxa = Math.abs(dx), dza = Math.abs(dz);
        double xs, xe, zs, ze;

        if(dx == 0 && dz == 0) {
        	placeBlock(v1.x, v1.z, maxHeight);
            return;
        }

        if(dx > 0) { xs = v1.x; xe = v2.x; }
        else /*dx < 0*/ { xs = v2.x; xe = v1.x; }
        if(dz > 0) { zs = v1.z; ze = v2.z; }
        else /*dz < 0*/ { zs = v2.z; ze = v1.z; }
        
        if(dxa == 0) {
        	double x = v1.x;
            for(double z = zs; z < ze; z++) placeBlock(x, z, maxHeight);
            placeBlock(x, ze, maxHeight);
        } else if(dza == 0) {
        	double z = v1.z;
            for(double x = xs; x < xe; x++) placeBlock(x, z, maxHeight);
            placeBlock(xe, z, maxHeight);
        } else if(dxa > dza) {
            for(double x = xs; x < xe; x++) placeBlock(x, v1.z + dz * (x - v1.x) / dx, maxHeight);
            placeBlock(xe, v1.z + dz * (xe - v1.x) / dx, maxHeight);
        } else {
            for(double z = zs; z < ze; z++) placeBlock(v1.x + dx * (z - v1.z) / dz, z, maxHeight);
        	placeBlock(v1.x + dx * (ze - v1.z) / dz, ze, maxHeight);
        }
    }

    
    
    public int getMaxHeightOfTheLine(Vector2DH v1, Vector2DH v2) {

        double dx = v2.x - v1.x, dz = v2.z - v1.z;
        double dxa = Math.abs(dx), dza = Math.abs(dz);
        double xs, xe, zs, ze;
        int yMax = -10000, yTemp;

        if(dx == 0 && dz == 0) {
            return getYFunction.apply(v1);
        }

        if(dx > 0) { xs = v1.x; xe = v2.x; }
        else /*dx < 0*/ { xs = v2.x; xe = v1.x; }
        if(dz > 0) { zs = v1.z; ze = v2.z; }
        else /*dz < 0*/ { zs = v2.z; ze = v1.z; }
        
        if(dxa == 0) {
        	double x = v1.x;
            for(double z = zs; z < ze; z++) {
            	yMax = yMax < (yTemp = getYFunction.apply(new Vector2DH(x, z))) ? yTemp : yMax;
            }
        	yMax = yMax < (yTemp = getYFunction.apply(new Vector2DH(x, ze))) ? yTemp : yMax;
        } else if(dza == 0) {
        	double z = v1.z;
            for(double x = xs; x < xe; x++) {
            	yMax = yMax < (yTemp = getYFunction.apply(new Vector2DH(x, z))) ? yTemp : yMax;
            }
        	yMax = yMax < (yTemp = getYFunction.apply(new Vector2DH(xe, z))) ? yTemp : yMax;
        } else if(dxa > dza) {
            for(double x = xs; x < xe; x++) {
            	yMax = yMax < (yTemp = getYFunction.apply(new Vector2DH(x, v1.z + dz * (x - v1.x) / dx))) ? yTemp : yMax;
            }
        	yMax = yMax < (yTemp = getYFunction.apply(new Vector2DH(xe, v1.z + dz * (xe - v1.x) / dx))) ? yTemp : yMax;
        } else {
            for(double z = zs; z < ze; z++) {
            	yMax = yMax < (yTemp = getYFunction.apply(new Vector2DH(v1.x + dx * (z - v1.z) / dz, z))) ? yTemp : yMax;
            }
        	yMax = yMax < (yTemp = getYFunction.apply(new Vector2DH(v1.x + dx * (ze - v1.z) / dz, ze))) ? yTemp : yMax;
        }
        
        return yMax;
    }
    
    
    
    private void placeBlock(double x, double z) {
    	if(!region.contains(new Vector(Math.floor(x), region.getMinimumY(), Math.floor(z)))) return;
    	int y = getYFunction.apply(new Vector2DH(x, z));
    	world.setBlockState(new BlockPos(x, y, z), state);
    }

    
    
    private void placeBlock(double x, double z, int maxHeight) {
    	if(!region.contains(new Vector(Math.floor(x), region.getMinimumY(), Math.floor(z)))) return;
    	int terrainY = getYFunction.apply(new Vector2DH(x, z));
    	for(int y = terrainY; y <= maxHeight; ++y) world.setBlockState(new BlockPos(x, y, z), state);
    }

}
