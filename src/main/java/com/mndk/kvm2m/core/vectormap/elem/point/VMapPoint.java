package com.mndk.kvm2m.core.vectormap.elem.point;

import org.kabeja.dxf.DXFPoint;

import com.mndk.kvm2m.core.projection.Grs80Projection;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vectormap.VMapObjectType;
import com.mndk.kvm2m.core.vectormap.elem.VMapElement;
import com.mndk.ngiparser.ngi.element.NgiPointElement;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VMapPoint extends VMapElement {

	private final Vector2DH point;

    public VMapPoint(DXFPoint point, Grs80Projection projection, VMapObjectType type) {
    	super(type);
        this.point = projectGrs80CoordToBteCoord(projection, point.getX(), point.getY());
    }

    public VMapPoint(NgiPointElement point, Grs80Projection projection, VMapObjectType type) {
    	super(type);
        this.point = projectGrs80CoordToBteCoord(projection, point.position.getAxis(0), point.position.getAxis(1));
    }
    
    public Vector2DH getPosition() {
    	return this.point;
    }

	@Override
	public void generateBlocks(FlatRegion region, World world, TriangleList triangles) {
		if(this.getType().getBlockState() == null) return;
		if(region.contains(point.withHeight(region.getMinimumY()).toIntegerWorldEditVector())) {
			Vector2DH p = point.withHeight(triangles.interpolateHeight(point));
			world.setBlockState(new BlockPos(p.x, p.height, p.z), this.getType().getBlockState());
		}
	}
	
}