package com.mndk.kvm2m.core.util.shape;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Triangle {
	
	
	
	public final Vector2DH v1, v2, v3;
	public final int minX, minZ, maxX, maxZ;

	
	
	public Triangle(Vector2DH a, Vector2DH b, Vector2DH c) {
		this.v1 = a; this.v2 = b; this.v3 = c;
		this.minX = (int) Math.floor(Math.min(a.x, Math.min(b.x, c.x)));
		this.minZ = (int) Math.floor(Math.min(a.z, Math.min(b.z, c.z)));
		this.maxX = (int) Math.ceil(Math.max(a.x, Math.max(b.x, c.x)));
		this.maxZ = (int) Math.ceil(Math.max(a.z, Math.max(b.z, c.z)));
	}
	
	
	
	public Vector contains(Vector2D point) {
		return contains(point.getX(), point.getZ());
	}
	
	public Vector contains(Vector p) {
		return contains(p.getX(), p.getZ());
	}
	
	public Vector contains(Vector2DH v) {
		return contains(v.x, v.z);
	}
	
	public Vector contains(double x, double z) {
		/* Bounding box test first, for quick rejections. */
		if ((x < v1.x && x < v2.x && x < v3.x) ||
			(x > v1.x && x > v2.x && x > v3.x) ||
			(z < v1.z && z < v2.z && z < v3.z) ||
			(z > v1.z && z > v2.z && z > v3.z))
			
			return null;

		double a = v2.x - v1.x,
			   b = v3.x - v1.x,
			   c = v2.z - v1.z,
			   d = v3.z - v1.z,
			   i = a * d - b * c;

		/* Degenerate tri. */
		if(i == 0.0)
			return null;

		double u = (d * (x - v1.x) - b * (z - v1.z)) / i,
			   v = (a * (z - v1.z) - c * (x - v1.x)) / i;

		/* If we're outside the tri, fail. */
		if(u < 0.0 || v < 0.0 || (u + v) > 1.0)
			return null;

		return new Vector(u, 0, v);
	}
	
	
	
	public void rasterize(World world, FlatRegion region, IBlockState blockState) {
		for(int z = minZ; z <= maxZ; ++z)  for(int x = minX; x <= maxX; ++x) {
			if(this.contains(x + .5, z + .5) == null || !region.contains(new Vector(x, region.getMinimumY(), z))) continue;
			int height = (int) Math.round(interpolateY(x, z));
			world.setBlockState(new BlockPos(x, height, z), blockState);
		}
	}
	
	
	
	private static final IBlockState AIR_STATE = Blocks.AIR.getDefaultState();
	public void removeTerrainAbove(World world, FlatRegion region) {
		for(int z = minZ; z <= maxZ; ++z)  for(int x = minX; x <= maxX; ++x) {
			if(this.contains(x + .5, z + .5) == null || !region.contains(new Vector(x, region.getMinimumY(), z))) continue;
			int height = (int) Math.round(interpolateY(x, z));
			for(int y = height + 1; world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.AIR; y++) {
				world.setBlockState(new BlockPos(x, y, z), AIR_STATE);
			}
		}
	}



	private static final IBlockState DIRT_STATE = Blocks.DIRT.getDefaultState();
	public void fillBlocksBelow(World world, FlatRegion region) {
		for(int z = minZ; z <= maxZ; ++z)  for(int x = minX; x <= maxX; ++x) {
			if(this.contains(x + .5, z + .5) == null || !region.contains(new Vector(x, region.getMinimumY(), z))) continue;
			int height = (int) Math.round(interpolateY(x, z));
			for(int y = height - 1; world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.STONE; y--) {
				world.setBlockState(new BlockPos(x, y, z), DIRT_STATE);
			}
		}
	}
	
	
	
	public double interpolateY(Vector2DH point) {
		return this.interpolateY(point.x, point.z);
	}
	
	
	
	public double interpolateY(double p_x, double p_z) {
		double x_1 = v1.x, x_2 = v2.x, x_3 = v3.x;
		double y_1 = v1.z, y_2 = v2.z, y_3 = v3.z;
		
		double denom = (y_2 - y_3) * (x_1 - x_3) + (x_3 - x_2) * (y_1 - y_3);
		double w_1 = ( (y_2 - y_3) * (p_x - x_3) + (x_3 - x_2) * (p_z - y_3) ) / denom;
		double w_2 = ( (y_3 - y_1) * (p_x - x_3) + (x_1 - x_3) * (p_z - y_3) ) / denom;
		double w_3 = 1 - w_1 - w_2;
		
		return v1.height * w_1 + v2.height * w_2 + v3.height * w_3;
	}
	
	
	
	@Override
	public String toString() {
		return "Triangle[" + v1 + ", " + v2 + ", " + v3 + "]";
	}
	
}
