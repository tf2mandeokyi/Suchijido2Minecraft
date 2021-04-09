package com.mndk.kvm2m.core.vectormap.elem.point;

import java.util.Map;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vectormap.VMapBlockSelector;
import com.mndk.kvm2m.core.vectormap.elem.VMapElement;
import com.mndk.kvm2m.core.vectormap.elem.VMapElementLayer;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VMapPoint extends VMapElement {

	private final Vector2DH point;

	public VMapPoint(VMapElementLayer layer, Vector2DH point, Map<String, Object> dataRow) {
		super(layer, dataRow);
		this.point = point;
	}

	public VMapPoint(VMapElementLayer layer, Vector2DH point, Object[] dataRow) {
		super(layer, dataRow);
		this.point = point;
	}
	
	public Vector2DH getPosition() {
		return this.point;
	}

	@Override
	public void generateBlocks(FlatRegion region, World world, TriangleList triangles) {

		IBlockState state = VMapBlockSelector.getBlockState(this);
		if(state == null) return;
		
		int y = VMapBlockSelector.getAdditionalHeight(this);
		
		if(region.contains(point.withHeight(region.getMinimumY()).toIntegerWorldEditVector())) {
			
			Vector2DH p = point.withHeight(triangles.interpolateHeight((int) Math.floor(point.x), (int) Math.floor(point.z)) + y);
			world.setBlockState(new BlockPos(p.x, p.height, p.z), state);
		}
	}
	
}