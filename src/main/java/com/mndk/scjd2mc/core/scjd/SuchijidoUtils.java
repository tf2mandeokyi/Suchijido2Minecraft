package com.mndk.scjd2mc.core.scjd;

import com.mndk.scjd2mc.core.projection.Korea2010BeltProjection;
import com.mndk.scjd2mc.core.projection.Projections;
import com.mndk.scjd2mc.core.scjd.elem.*;
import com.mndk.scjd2mc.core.scjd.geometry.*;
import com.mndk.scjd2mc.core.scjd.type.ElementDataType;
import com.mndk.scjd2mc.core.scjd.type.ElementGeometryType;
import com.mndk.scjd2mc.mod.event.ServerTickRepeater;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SuchijidoUtils {

	private static final Pattern generalMapIdPattern = Pattern.compile("^\\(.{4}\\)수치지도_(\\d+)_");


	public static String getMapIndexFromFileName(String fileName) {
		Matcher matcher = generalMapIdPattern.matcher(fileName);
		if(matcher.find()) {
			return matcher.group(1);
		}
		return fileName;
	}


	public static Korea2010BeltProjection getProjectionFromMapName(String fileName) {

		fileName = getMapIndexFromFileName(fileName);
		char number = fileName.charAt(2);

		if(number == '5') {
			return Projections.KOREA2010_WEST;
		} else if(number == '6' || number == '7') {
			return Projections.KOREA2010_CENTRAL;
		} else if(number == '8' || number == '9') {
			return Projections.KOREA2010_EAST;
		} else if(number == '0' || number == '1'){
			return Projections.KOREA2010_EASTSEA;
		}
		return null;
	}


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
