package com.mndk.kvm2m.core.vectormap;

import com.mndk.kvm2m.core.vectormap.elem.VMapElement;
import com.mndk.kvm2m.core.vectormap.elem.poly.VMapBuilding;

import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;

public class VMapBlockSelector {

	public static IBlockState getBlockState(VMapElement element) {
		
		switch(element.getParent().getType()) {
			// A types
			case 도로경계: 
				return Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY);
			case 입체교차부:
				return Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.SILVER);
			case 보도: 
			case 안전지대:
			case 승강장:
				if("어린이보호구역".equals(element.getDataByColumn("구조"))) return null;
				return Blocks.DOUBLE_STONE_SLAB.getDefaultState();
			case 횡단보도: 
				return Blocks.CONCRETE.getDefaultState();
			case 육교:
			case 교량:
				return Blocks.IRON_BLOCK.getDefaultState();
			case 승강장_지붕:
				return Blocks.WOOL.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.SILVER);
			case 터널: 
				return Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.MAGENTA);
			case 철도:
				return Blocks.ANVIL.getDefaultState();
			
			// B types
			case 건물: 
				return Blocks.STONE.getDefaultState();
			case 담장:
				return Blocks.OAK_FENCE.getDefaultState();
			
			// C types
			case 조명:
				return Blocks.REDSTONE_LAMP.getDefaultState();
			case 신호등:
				return Blocks.NETHER_BRICK_FENCE.getDefaultState();
				
			// D types
			case 독립수:
				return Blocks.LOG.getDefaultState();
				
			// E types
			case 하천중심선:
			case 등심선: 
				return Blocks.LAPIS_BLOCK.getDefaultState();
			case 하천경계:
			case 호수:
			case 해안선:
				return Blocks.WATER.getDefaultState();
				
			// F types
			case 절토:
				return Blocks.END_BRICKS.getDefaultState();
			case 옹벽:
				return Blocks.BRICK_BLOCK.getDefaultState();
				
			// G types
			case 읍면동_행정경계:
				if("기타콘크리트구조물".equals(element.getDataByColumn("용도")))
					return Blocks.CONCRETE.getDefaultState();
					
			
			default:
				return null;
		}
		
	}
	
	public static int getAdditionalHeight(VMapElement element) {
		
		switch(element.getParent().getType()) {
			case 옹벽:
				return (int) Math.round((Double) element.getDataByColumn("높이"));
			case 건물:
				return (Integer) element.getDataByColumn("층수") * VMapBuilding.FLOOR_HEIGHT;
			case 등고선:
				return (int) Math.round((Double) element.getDataByColumn("등고수치"));
			case 표고점:
				return (int) Math.round((Double) element.getDataByColumn("수치"));
			case 담장:
			case 터널입구:
			case 철도: case 승강장:
			case 조명: case 신호등:
			case 독립수:
			case 절토:
				return 1;
			case 승강장_지붕:
				return 2;
			case 육교:
			case 입체교차부:
				return 3;
			default:
				return 0;
		}
	}
	
}
