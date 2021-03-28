package com.mndk.kvm2m.core.vectormap;

import com.mndk.kvm2m.core.vectormap.elem.VMapElement;
import com.mndk.ngiparser.ngi.element.NgiElement;

import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;

public class VMapBlockSelector {

	public static IBlockState getBlockState(NgiElement<?> element, VMapElementType type) {
		
		switch(type) {
			// A types
			case 도로경계: 
				return Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY);
			case 보도: 
			case 안전지대:
			case 승강장:
				if("어린이보호구역".equals(element.getRowData("구조"))) return null;
				return Blocks.DOUBLE_STONE_SLAB.getDefaultState();
			case 횡단보도: 
				return Blocks.CONCRETE.getDefaultState();
			case 육교: 
				return Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.SILVER);
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
			
			default:
				return null;
		}
		
	}
	
	public static int getAdditionalHeight(VMapElement element) {
		
		VMapElementType type = element.getParent().getType();
		
		switch(type) {
			case 담장:
			case 터널입구:
			case 철도: case 승강장:
			case 조명: case 신호등:
			case 독립수:
				return 1;
			case 육교:
			case 입체교차부:
				return 2;
			default:
				return 0;
		}
	}
	
}
