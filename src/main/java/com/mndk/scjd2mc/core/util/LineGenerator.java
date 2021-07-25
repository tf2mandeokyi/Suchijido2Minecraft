package com.mndk.scjd2mc.core.util;

import com.mndk.scjd2mc.core.util.math.Vector2DH;
import com.mndk.scjd2mc.core.scjd.SuchijidoUtils;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.FlatRegion;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.BiFunction;

public abstract class LineGenerator {
	

	public final BiFunction<Integer, Integer, Integer> getYFunction;
	public final World world;
	public final FlatRegion region;
	public final IBlockState state;

	
	
	public LineGenerator(BiFunction<Integer, Integer, Integer> getYFunction, World world, FlatRegion region, IBlockState state) {
		this.getYFunction = getYFunction;
		this.world = world;
		this.region = region;
		this.state = state;
	}
	
	
	
	public final void generate(Vector2DH v1, Vector2DH v2) {

		double dx = v2.x - v1.x, dz = v2.z - v1.z;
		double dxa = Math.abs(dx), dza = Math.abs(dz);
		double xs, xe, zs, ze;

		if(dx == 0 && dz == 0) {
			this.generatePoint(v1.x, v1.z);
			return;
		}

		if(dx > 0) { xs = v1.x; xe = v2.x; }
		else /*dx < 0*/ { xs = v2.x; xe = v1.x; }
		if(dz > 0) { zs = v1.z; ze = v2.z; }
		else /*dz < 0*/ { zs = v2.z; ze = v1.z; }
		
		if(dxa == 0) {
			double x = v1.x;
			for(double z = zs; z < ze; z++) this.generatePoint(x, z);
			this.generatePoint(x, ze);
		} else if(dza == 0) {
			double z = v1.z;
			for(double x = xs; x < xe; x++) this.generatePoint(x, z);
			this.generatePoint(xe, z);
		} else if(dxa > dza) {
			for(double x = xs; x < xe; x++) this.generatePoint(x, v1.z + dz * (x - v1.x) / dx);
			this.generatePoint(xe, v1.z + dz * (xe - v1.x) / dx);
		} else {
			for(double z = zs; z < ze; z++) this.generatePoint(v1.x + dx * (z - v1.z) / dz, z);
			this.generatePoint(v1.x + dx * (ze - v1.z) / dz, ze);
		}
	}
	
	
	
	protected int getHeight(double x, double z) {
		return getYFunction.apply((int) Math.floor(x), (int) Math.floor(z));
	}

	
	
	protected abstract void generatePoint(double x, double z);
	
	
	
	public static class TerrainLine extends LineGenerator {
		public TerrainLine(BiFunction<Integer, Integer, Integer> getYFunction, World world, FlatRegion region, IBlockState state) {
			super(getYFunction, world, region, state);
		}

		@Override
		protected void generatePoint(double x, double z) {
			if(!region.contains(new Vector(Math.floor(x), region.getMinimumY(), Math.floor(z)))) return;
			int y = getHeight(x, z);
			SuchijidoUtils.setBlock(world, new BlockPos(x, y, z), state);
		}
	}
	
	
	
	public static class BuildingWall extends LineGenerator {
		
		private int maxHeight;
		
		public BuildingWall(BiFunction<Integer, Integer, Integer> getYFunction, World world, FlatRegion region, IBlockState state, int maxHeight) {
			super(getYFunction, world, region, state);
			this.maxHeight = maxHeight;
		}

		@Override
		protected void generatePoint(double x, double z) {
			if(!region.contains(new Vector(Math.floor(x), region.getMinimumY(), Math.floor(z)))) return;
			int terrainY = getHeight(x, z);
			for(int y = terrainY; y <= maxHeight; ++y) SuchijidoUtils.setBlock(world, new BlockPos(x, y, z), state);
		}
	}
	
	
	
	public static class TerrainWall extends LineGenerator {
		
		private int height;
		
		public TerrainWall(BiFunction<Integer, Integer, Integer> getYFunction, World world, FlatRegion region, IBlockState state, int height) {
			super(getYFunction, world, region, state);
			this.height = height;
		}

		@Override
		protected void generatePoint(double x, double z) {
			if(!region.contains(new Vector(Math.floor(x), region.getMinimumY(), Math.floor(z)))) return;
			int terrainY = getHeight(x, z);
			for(int y = terrainY; y <= terrainY + height; ++y) SuchijidoUtils.setBlock(world, new BlockPos(x, y, z), state);
		}
	}
	
	
	
	public static class MeasureHeight extends LineGenerator {
		
		public MeasureHeight(BiFunction<Integer, Integer, Integer> getYFunction) {
			super(getYFunction, null, null, null);
		}

		public int getMaxHeight(Vector2DH v1, Vector2DH v2) {

			double dx = v2.x - v1.x, dz = v2.z - v1.z;
			double dxa = Math.abs(dx), dza = Math.abs(dz);
			double xs, xe, zs, ze;
			int yMax = -10000, yTemp;

			if(dx == 0 && dz == 0) {
				return getHeight(v1.x, v1.z);
			}

			if(dx > 0) { xs = v1.x; xe = v2.x; }
			else /*dx < 0*/ { xs = v2.x; xe = v1.x; }
			if(dz > 0) { zs = v1.z; ze = v2.z; }
			else /*dz < 0*/ { zs = v2.z; ze = v1.z; }
			
			if(dxa == 0) {
				double x = v1.x;
				for(double z = zs; z < ze; z++) {
					yMax = yMax < (yTemp = getHeight(x, z)) ? yTemp : yMax;
				}
				yMax = yMax < (yTemp = getHeight(x, ze)) ? yTemp : yMax;
			} else if(dza == 0) {
				double z = v1.z;
				for(double x = xs; x < xe; x++) {
					yMax = yMax < (yTemp = getHeight(x, z)) ? yTemp : yMax;
				}
				yMax = yMax < (yTemp = getHeight(xe, z)) ? yTemp : yMax;
			} else if(dxa > dza) {
				for(double x = xs; x < xe; x++) {
					yMax = yMax < (yTemp = getHeight(x, v1.z + dz * (x - v1.x) / dx)) ? yTemp : yMax;
				}
				yMax = yMax < (yTemp = getHeight(xe, v1.z + dz * (xe - v1.x) / dx)) ? yTemp : yMax;
			} else {
				for(double z = zs; z < ze; z++) {
					yMax = yMax < (yTemp = getHeight(v1.x + dx * (z - v1.z) / dz, z)) ? yTemp : yMax;
				}
				yMax = yMax < (yTemp = getHeight(v1.x + dx * (ze - v1.z) / dz, ze)) ? yTemp : yMax;
			}
			
			return yMax;
		}

		@Override
		protected void generatePoint(double x, double z) {}
	}

}
