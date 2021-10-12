package com.mndk.scjd2mc.core.scjd.elem;

import com.mndk.scjd2mc.core.scjd.SuchijidoUtils;
import com.mndk.scjd2mc.core.scjd.geometry.Point;
import com.mndk.scjd2mc.core.scjd.type.ElementStyleSelector;
import com.mndk.scjd2mc.core.util.math.Vector2DH;
import com.mndk.scjd2mc.core.util.shape.TriangleList;
import com.sk89q.worldedit.regions.FlatRegion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.IOException;

public class ScjdElevationPoint extends ScjdElement<Point> {
	
	public final int y;

	public ScjdElevationPoint(ScjdLayer layer, Point point, Object[] rowData) throws Exception {
		super(layer, "", point, rowData);
		Object value = this.getData("수치");
		if(value instanceof String) {
			try {
				value = Double.parseDouble((String) value);
			} catch(NumberFormatException e) {
				throw new IOException("Invalid 수치 data: \"" + value + "\"");
			}
		}
		this.shape.getShape().height = this.y = (int) Math.round(((Number) value).doubleValue());
	}

	@Override
	public void generateBlocks(FlatRegion region, World world, TriangleList triangles) {

		ElementStyleSelector.ScjdElementStyle[] styles = ElementStyleSelector.getStyle(this);
		if(styles == null) return;
		for(ElementStyleSelector.ScjdElementStyle style : styles) {
			if(style == null) return; if(style.state == null) return;

			Vector2DH p = shape.getShape();

			if(region.contains(p.withHeight(region.getMinimumY()).toIntegerWorldEditVector())) {
				SuchijidoUtils.setBlock(world, new BlockPos(p.x, this.y, p.z), style.state);
			}
		}
	}

	@Override
	public String toString() {
		return "VMapElevationPoint{pos=" + shape.getShape() + "}";
	}

}
