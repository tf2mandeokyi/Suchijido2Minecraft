package com.mndk.scjd2mc.core.scjd;

import com.mndk.scjd2mc.core.projection.Korea2010BeltProjection;
import com.mndk.scjd2mc.core.projection.Projections;
import com.mndk.scjd2mc.mod.event.ServerTickRepeater;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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

}
