package com.mndk.kvm2m.core.vmap.elem.point;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vmap.VMapElementStyleSelector;
import com.mndk.kvm2m.core.vmap.elem.VMapElementLayer;
import com.sk89q.worldedit.regions.FlatRegion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VMapElevationPoint extends VMapPoint {
	
	public final int y;
	
	public VMapElevationPoint(VMapElementLayer layer, Vector2DH point, Object[] rowData) {
		super(layer, point, rowData);
		this.y = VMapElementStyleSelector.getStyle(this)[0].y;
	}
	
	public Vector2DH toVector() {
		return this.getPosition().withHeight(this.y);
	}

	@Override
	public void generateBlocks(FlatRegion region, World world, TriangleList triangles) {

		VMapElementStyleSelector.VMapElementStyle[] styles = VMapElementStyleSelector.getStyle(this);
		if(styles == null) return;
		for(VMapElementStyleSelector.VMapElementStyle style : styles) {
			if(style == null) return; if(style.state == null) return;

			Vector2DH p = this.getPosition();

			if(region.contains(p.withHeight(region.getMinimumY()).toIntegerWorldEditVector())) {
				world.setBlockState(new BlockPos(p.x, this.y, p.z), style.state);
			}
		}
	}
	
	@Override
	public String toString() {
		return "VMapElevationPoint{pos=" + this.toVector() + "}";
	}

}
