package com.mndk.kvm2m.core.vectormap;

import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;

public class VMapBlockSelector {

	static IBlockState getBlockState(VMapElementType type) {
		
		if(type.layerHeader.startsWith("G") || type.layerHeader.startsWith("H")) {
			return null;
		}
		switch(type) {
			// A types
			case 도로경계: 
				return Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY);
			case 보도: 
			case 안전지대:
				return Blocks.DOUBLE_STONE_SLAB.getDefaultState();
			case 횡단보도: 
				return Blocks.CONCRETE.getDefaultState();
			case 육교: 
				return Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.SILVER);
			case 터널: 
				return Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.MAGENTA);
			
			// B types
			case 건물: 
				return Blocks.STONE.getDefaultState();
			case 담장:
				return Blocks.OAK_FENCE.getDefaultState();
			
			// E types
			case 하천중심선:
			case 등심선: 
				return Blocks.LAPIS_BLOCK.getDefaultState();
			case 하천경계:
			case 호수:
			case 해안선:
				return Blocks.WATER.getDefaultState();
			
			default:
				return null;
		}
		
	}
	
}
