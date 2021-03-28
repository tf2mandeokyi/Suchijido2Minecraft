package com.mndk.kvm2m.core.vectormap.elem;

import com.mndk.kvm2m.core.projection.Grs80Projection;
import com.mndk.kvm2m.core.projection.Projections;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vectormap.VMapElementType;
import com.sk89q.worldedit.regions.FlatRegion;

import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.minecraft.world.World;

public abstract class VMapElement {

	
	private final VMapElementType type;
	
	
	public VMapElement(VMapElementType type) {
		this.type = type;
	}
	
	
	protected static Vector2DH projectGrs80CoordToBteCoord(Grs80Projection projection, double x, double y) {
		double[] geoCoordinate = projection.toGeo(x, y), bteCoordinate;
		try {
			bteCoordinate = Projections.BTE.fromGeo(geoCoordinate[0], geoCoordinate[1]);
		} catch(OutOfProjectionBoundsException exception) {
			throw new RuntimeException(exception); // wcpgw lmao
		}
		return new Vector2DH(bteCoordinate[0] * Projections.BTE.metersPerUnit(), -bteCoordinate[1] * Projections.BTE.metersPerUnit());
	}
	
	
	public VMapElementType getType() {
		return type;
	}
	
	
	public abstract void generateBlocks(FlatRegion region, World world, TriangleList triangles);
}
