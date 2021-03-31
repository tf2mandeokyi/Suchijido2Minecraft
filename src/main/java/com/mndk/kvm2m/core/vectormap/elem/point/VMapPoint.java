package com.mndk.kvm2m.core.vectormap.elem.point;

import com.mndk.kvm2m.core.projection.Grs80Projection;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vectormap.VMapBlockSelector;
import com.mndk.kvm2m.core.vectormap.elem.VMapElement;
import com.mndk.kvm2m.core.vectormap.elem.VMapElementLayer;
import com.mndk.ngiparser.ngi.element.NgiPointElement;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VMapPoint extends VMapElement {

	private final Vector2DH point;

	/*
	public VMapPoint(DXFPoint point, Grs80Projection projection, VMapElementType type) {
		super(type);
		this.point = projectGrs80CoordToBteCoord(projection, point.getX(), point.getY());
	}
	*/

	public VMapPoint(VMapElementLayer layer, NgiPointElement point, IBlockState blockState, Grs80Projection projection) {
		super(layer, blockState);
		this.point = projectGrs80CoordToBteCoord(projection, point.position.getAxis(0), point.position.getAxis(1));
	}
	
	public Vector2DH getPosition() {
		return this.point;
	}

	@Override
	public void generateBlocks(FlatRegion region, World world, TriangleList triangles) {
		if(this.blockState == null) return;
		
		if(region.contains(point.withHeight(region.getMinimumY()).toIntegerWorldEditVector())) {
			int y = VMapBlockSelector.getAdditionalHeight(this);
			
			Vector2DH p = point.withHeight(triangles.interpolateHeight(point) + y);
			world.setBlockState(new BlockPos(p.x, p.height, p.z), this.blockState);
		}
	}
	
}