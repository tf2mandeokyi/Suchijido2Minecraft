package com.mndk.kvm2m.core.vmap;

import com.mndk.kvm2m.core.projection.Korea2010BeltProjection;
import com.mndk.kvm2m.core.projection.Projections;
import com.mndk.kvm2m.mod.event.ServerTickRepeater;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VMapUtils {

	private static final Pattern generalMapIdPattern = Pattern.compile("^\\(.{4}\\)수치지도_(\\d+)");

	public static Korea2010BeltProjection getProjectionFromMapName(String fileName) {

		Matcher matcher = generalMapIdPattern.matcher(fileName);
		if(matcher.find()) {
			fileName = matcher.group(1);
		}

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



	public static int getScaleFromMapId(String id) {
		switch(id.length()) {
			case 3: return 250000;
			case 5: return 50000;
			case 6: return 25000;
			case 7: return 10000;
			case 8:
				char last = id.charAt(7);
				if(last >= '0' && last <= '9') // If the last character is v1 number:
					return 5000;
				else // Or else if it's an alphabet
					return 2500;
			case 9: return 1000;
			case 10: return 500;
		}
		return -1;
	}

}
