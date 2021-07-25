package com.mndk.scjd2mc.core.util.shape;

import com.mndk.scjd2mc.core.util.math.Vector2DH;
import com.mndk.scjd2mc.core.scjd.SuchijidoUtils;
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

			int height = (int) Math.round(interpolateY(x + .5, z + .5));

			SuchijidoUtils.setBlock(world, new BlockPos(x, height, z), blockState);
		}
	}
	
	
	
	public void removeTerrainAbove(World world, FlatRegion region) {
		for(int z = minZ; z <= maxZ; ++z)  for(int x = minX; x <= maxX; ++x) {
			if(this.contains(x + .5, z + .5) == null || !region.contains(new Vector(x, region.getMinimumY(), z))) continue;

			int height = (int) Math.round(interpolateY(x + .5, z + .5));

			for(int y = height + 1; world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.AIR; y++) {
				SuchijidoUtils.setBlock(world, new BlockPos(x, y, z), Blocks.AIR.getDefaultState());
			}
		}
	}



	public void fillBlocksBelow(World world, FlatRegion region) {
		for(int z = minZ; z <= maxZ; ++z)  for(int x = minX; x <= maxX; ++x) {
			if(this.contains(x + .5, z + .5) == null || !region.contains(new Vector(x, region.getMinimumY(), z))) continue;

			int height = (int) Math.round(interpolateY(x + .5, z + .5));

			SuchijidoUtils.setBlock(world, new BlockPos(x, height - 1, z), Blocks.DIRT.getDefaultState());
			SuchijidoUtils.setBlock(world, new BlockPos(x, height - 2, z), Blocks.DIRT.getDefaultState());

			for(int y = height - 3; world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.STONE; y--) {
				SuchijidoUtils.setBlock(world, new BlockPos(x, y, z), Blocks.STONE.getDefaultState());
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
	
	
	
	private static double side(Vector2DH p, Vector2DH q, Vector2DH a, Vector2DH b) {
	    double z1 = (b.x - a.x) * (p.z - a.z) - (p.x - a.x) * (b.z - a.z);
	    double z2 = (b.x - a.x) * (q.z - a.z) - (q.x - a.x) * (b.z - a.z);
	    return z1 * z2;
	}
	
	
	
//	public boolean checkIntersection(Vector2DH p0, Vector2DH p1) {
//		/* Check whether segment is outside one of the three half-planes
//	     * delimited by the triangle. */
//		double f1 = side(p0, v3, v1, v2), f2 = side(p1, v3, v1, v2);
//		double f3 = side(p0, v1, v2, v3), f4 = side(p1, v1, v2, v3);
//		double f5 = side(p0, v2, v3, v1), f6 = side(p1, v2, v3, v1);
//	    /* Check whether triangle is totally inside one of the two half-planes
//	     * delimited by the segment. */
//		double f7 = side(v1, v2, p0, p1);
//		double f8 = side(v2, v3, p0, p1);
//
//	    /* If segment is strictly outside triangle, or triangle is strictly
//	     * apart from the line, we're not intersecting */
//	    if ((f1 < 0 && f2 < 0) || (f3 < 0 && f4 < 0) || (f5 < 0 && f6 < 0)
//	          || (f7 > 0 && f8 > 0))
//	        return false;
//
//	    /* If segment is aligned with one of the edges, we're overlapping */
//	    if ((f1 == 0 && f2 == 0) || (f3 == 0 && f4 == 0) || (f5 == 0 && f6 == 0))
//	        return false;
//
//	    /* If segment is outside but not strictly, or triangle is apart but
//	     * not strictly, we're touching */
//	    if ((f1 <= 0 && f2 <= 0) || (f3 <= 0 && f4 <= 0) || (f5 <= 0 && f6 <= 0)
//	          || (f7 >= 0 && f8 >= 0))
//	        return false;
//
//	    /* If both segment points are strictly inside the triangle, we
//	     * are not intersecting either */
//	    if (f1 > 0 && f2 > 0 && f3 > 0 && f4 > 0 && f5 > 0 && f6 > 0)
//	        return false;
//
//	    /* Otherwise we're intersecting with at least one edge */
//	    return true;
//	}
	
	
	
	@Override
	public String toString() {
		return "Triangle[" + v1 + ", " + v2 + ", " + v3 + "]";
	}



	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Triangle)) return false;
		Triangle t = (Triangle) o;
		return this.v1.equalsXZ(t.v1) && this.v2.equalsXZ(t.v2) && this.v3.equalsXZ(t.v3);
	}
	
}
