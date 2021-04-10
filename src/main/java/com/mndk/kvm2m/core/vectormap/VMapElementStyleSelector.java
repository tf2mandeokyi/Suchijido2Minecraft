package com.mndk.kvm2m.core.vectormap;

import com.mndk.kvm2m.core.vectormap.elem.VMapElement;
import com.mndk.kvm2m.core.vectormap.elem.poly.VMapBuilding;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;

public class VMapElementStyleSelector {

	public static VMapElementStyle getStyle(VMapElement element) {
		
		int height;
		
		switch(element.getParent().getType()) {
			// A types
			case 도로경계: 
				return new VMapElementStyle(Blocks.CONCRETE, EnumDyeColor.GRAY, 0);
			case 입체교차부:
				return new VMapElementStyle(Blocks.CONCRETE, EnumDyeColor.SILVER, 3);
			case 보도: 
			case 안전지대:
				if("어린이보호구역".equals(element.getDataByColumn("구조"))) return null;
				return new VMapElementStyle(Blocks.DOUBLE_STONE_SLAB, 0);
			case 승강장:
				return new VMapElementStyle(Blocks.DOUBLE_STONE_SLAB, 1);
			case 횡단보도: 
				return new VMapElementStyle(Blocks.CONCRETE, 0);
			case 육교:
				return new VMapElementStyle(Blocks.IRON_BLOCK, 3);
			case 교량:
				return new VMapElementStyle(Blocks.IRON_BLOCK, 0);
			case 승강장_지붕:
				return new VMapElementStyle(Blocks.WOOL, EnumDyeColor.SILVER, 2);
			case 터널: 
				return new VMapElementStyle(Blocks.CONCRETE, EnumDyeColor.MAGENTA, 0);
			case 철도:
				return new VMapElementStyle(Blocks.ANVIL, 1);
			
			// B types
			case 건물:
				Object floorCount = element.getDataByColumn("층수");
				if(floorCount instanceof Integer) height = (Integer) element.getDataByColumn("층수") * VMapBuilding.FLOOR_HEIGHT;
				else height = (int) Math.round((Double) element.getDataByColumn("층수")) * VMapBuilding.FLOOR_HEIGHT;
				return new VMapElementStyle(Blocks.STONE, height);
			case 담장:
				return new VMapElementStyle(Blocks.OAK_FENCE, 1);
			
			// C types
			case 조명:
				return new VMapElementStyle(Blocks.REDSTONE_LAMP, 1);
			case 신호등:
				return new VMapElementStyle(Blocks.NETHER_BRICK_FENCE, 1);
			case 도로분리대:
				return new VMapElementStyle(Blocks.CONCRETE, 1);
				
			// D types
			case 독립수:
				return new VMapElementStyle(Blocks.LOG, 1);
				
			// E types
			case 하천중심선:
			case 등심선: 
				return new VMapElementStyle(Blocks.LAPIS_BLOCK, 0);
			case 실폭하천:
			case 호수:
			case 해안선:
				return new VMapElementStyle(Blocks.WATER, 0);
				
			// F types
			case 등고선:
				return new VMapElementStyle(null, (int) Math.round((Double) element.getDataByColumn("등고수치")));
			case 표고점:
				return new VMapElementStyle(null, (int) Math.round((Double) element.getDataByColumn("수치")));
			case 절토:
				return new VMapElementStyle(Blocks.END_BRICKS, 1);
			case 옹벽:
				height = (int) Math.round((Double) element.getDataByColumn("높이"));
				return new VMapElementStyle(Blocks.BRICK_BLOCK, height);
				
			// G types
			case 기타경계:
				if("기타콘크리트구조물".equals(element.getDataByColumn("용도")))
					return new VMapElementStyle(Blocks.CONCRETE, 0);
					
			
			default:
				return null;
		}
		
	}
	
	public static class VMapElementStyle {
		public final IBlockState state;
		public final int y;
		public VMapElementStyle(Block block, int height) {
			this.state = block == null ? null : block.getDefaultState(); this.y = height;
		}
		public VMapElementStyle(Block block, EnumDyeColor color, int height) {
			this.state = block.getDefaultState().withProperty(BlockColored.COLOR, color); this.y = height;
		}
	}
	
}
