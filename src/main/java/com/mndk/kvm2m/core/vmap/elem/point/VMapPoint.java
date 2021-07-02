package com.mndk.kvm2m.core.vmap.elem.point;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vmap.VMapElementStyleSelector;
import com.mndk.kvm2m.core.vmap.VMapElementStyleSelector.VMapElementStyle;
import com.mndk.kvm2m.core.vmap.VMapUtils;
import com.mndk.kvm2m.core.vmap.elem.VMapElement;
import com.mndk.kvm2m.core.vmap.elem.VMapLayer;
import com.sk89q.worldedit.regions.FlatRegion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

public class VMapPoint extends VMapElement {

	private final Vector2DH point;

	public VMapPoint(VMapLayer layer, Vector2DH point, Map<String, Object> dataRow) {
		super(layer, dataRow);
		this.point = point;
	}

	public VMapPoint(VMapLayer layer, Vector2DH point, Object[] dataRow) {
		super(layer, dataRow);
		this.point = point;
	}
	
	public Vector2DH getPosition() {
		return this.point;
	}

	@Override
	public void generateBlocks(FlatRegion region, World world, TriangleList triangles) {

		VMapElementStyle[] styles = VMapElementStyleSelector.getStyle(this);
		if(styles == null) return;
		for(VMapElementStyle style : styles) {
			if(style == null) return; if(style.state == null) return;
			
			if(region.contains(point.withHeight(region.getMinimumY()).toIntegerWorldEditVector())) {
				
				Vector2DH p = point.withHeight(triangles.interpolateHeight((int) Math.floor(point.x), (int) Math.floor(point.z)) + style.y);
				VMapUtils.setBlock(world, new BlockPos(p.x, p.height, p.z), style.state);
			}
		}
	}
	
	
	@Override
	public String toString() {
		return "VMapPoint{type=" + parent.getType() + ",pos=" + this.point + "}";
	}
	
}