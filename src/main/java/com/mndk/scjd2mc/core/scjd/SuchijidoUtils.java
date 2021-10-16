package com.mndk.scjd2mc.core.scjd;

import com.mndk.scjd2mc.core.scjd.elem.*;
import com.mndk.scjd2mc.core.scjd.geometry.*;
import com.mndk.scjd2mc.core.scjd.type.ElementDataType;
import com.mndk.scjd2mc.core.scjd.type.ElementGeometryType;
import com.mndk.scjd2mc.mod.event.ServerTickRepeater;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SuchijidoUtils {


	public static void setBlock(World world, BlockPos pos, IBlockState state) {
		ServerTickRepeater.addTask(new ServerTickRepeater.BlockTask(world, pos, state));
	}


	public static List<ScjdElement<?>> combineGeometryAndData(
			@Nonnull ScjdLayer layer, GeometryShape<?> geometry, ElementDataType dataType, Object[] dataRow, String id,
			Map<String, String> options)
			throws Exception {

		ElementGeometryType geometryType = geometry.getType();

		switch(geometryType) {
			case POINT:
				if(dataType == ElementDataType.표고점) {
					return Collections.singletonList(new ScjdElevationPoint(layer, (Point) geometry, dataRow));
				}
				break;
			case LINESTRING:
				if(dataType == ElementDataType.등고선) {
					return Collections.singletonList(new ScjdContour(layer, (LineString) geometry, dataRow));
				}
				break;
			case MULTILINESTRING:
				if(dataType == ElementDataType.등고선) {
					MultiLineString mls = (MultiLineString) geometry;
					List<ScjdElement<?>> result = new ArrayList<>();
					for(LineString ls : mls.getShape()) { result.add(new ScjdContour(layer, ls, dataRow)); }
					return result;
				}
				break;
			case POLYGON:
				if(dataType == ElementDataType.건물) {
					return Collections.singletonList(new ScjdBuilding(layer, id, (Polygon) geometry, dataRow,
									options.containsKey("gen-building-shells")));
				}
				break;
			case MULTIPOLYGON:
				if(dataType == ElementDataType.건물) {
					MultiPolygon mp = (MultiPolygon) geometry;
					List<ScjdElement<?>> result = new ArrayList<>();
					for(Polygon p : mp.getShape()) { result.add(new ScjdBuilding(layer, id, p, dataRow,
							options.containsKey("gen-building-shells"))); }
					return result;
				}
				break;
		}

		return Collections.singletonList(new ScjdElement<>(layer, id, geometry, dataRow));
	}

}
