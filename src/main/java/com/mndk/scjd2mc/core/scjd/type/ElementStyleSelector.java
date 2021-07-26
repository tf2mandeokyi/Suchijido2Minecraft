package com.mndk.scjd2mc.core.scjd.type;

import com.mndk.scjd2mc.core.scjd.elem.ScjdElement;
import com.mndk.scjd2mc.core.scjd.elem.poly.ScjdBuilding;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;

public class ElementStyleSelector {
	
	
	
	public static ScjdElementStyle[] getStyle(ScjdElement element) {
		
		
		
		int height;
		
		switch(element.getParent().getType()) {
			// A types
			case 도로경계: 
				return singleStyle(Blocks.CONCRETE, EnumDyeColor.GRAY, 0);
			case 입체교차부:
				return singleStyle(Blocks.CONCRETE, EnumDyeColor.SILVER, 3);
			case 보도:
			case 안전지대:
				String type = (String) element.getData("구조");
				if(type != null && "어린이보호구역".startsWith(type)) {
					return null;
				}
				return singleStyle(Blocks.DOUBLE_STONE_SLAB, 0);
			case 승강장:
				return singleStyle(Blocks.DOUBLE_STONE_SLAB, 1);
			case 횡단보도:
				return singleStyle(Blocks.WOOL, EnumDyeColor.WHITE, 0);
			case 육교:
				return singleStyle(Blocks.IRON_BLOCK, 3);
			case 교량:
				return singleStyle(Blocks.IRON_BLOCK, 0);
			case 승강장_지붕:
				return singleStyle(Blocks.WOOL, EnumDyeColor.SILVER, 6);
			case 철도:
				return singleStyle(Blocks.ANVIL, 1);

			// B types
			case 건물:
				if(element instanceof ScjdBuilding) {
					Object floorCount = element.getData("층수");
					if(floorCount instanceof Integer) height = (Integer) floorCount;
					else height = (int) Math.round(((Number) floorCount).doubleValue());

					height = (int) (height * ScjdBuilding.FLOOR_HEIGHT);

					return singleStyle(Blocks.STONE, height);
				}
				else return singleStyle(Blocks.BRICK_BLOCK, 0);
			case 담장:
				return singleStyle(Blocks.OAK_FENCE, 1);

			// C types
			case 조명:
				return singleStyle(Blocks.REDSTONE_LAMP, 1);
			case 신호등:
				return singleStyle(Blocks.NETHER_BRICK_FENCE, 1);
			case 도로분리대:
				return singleStyle(Blocks.CONCRETE, 1);

			// D types
			case 독립수:
				return singleStyle(Blocks.LOG, 1);

			// E types
			case 실폭하천:
			case 호수:
			case 해안선:
				return doubleStyle(Blocks.AIR, 0, Blocks.WATER, -1);

			// F types
			case 등고선:
				return singleStyle(Blocks.DIAMOND_BLOCK,
						(int) Math.round(((Number) element.getData("등고수치")).doubleValue()));
			case 표고점:
				return singleStyle(Blocks.DIAMOND_BLOCK,
						(int) Math.round(((Number) element.getData("수치")).doubleValue()));
			case 절토:
				return singleStyle(Blocks.END_BRICKS, 1);
			case 옹벽:
				height = (int) Math.round(((Number) element.getData("높이")).doubleValue());
				return singleStyle(Blocks.END_BRICKS, height);

			// G types
			case 기타경계:
				if("기타콘크리트구조물".equals(element.getData("용도"))) {
					return singleStyle(Blocks.CONCRETE, 0);
				}
					
			
			default:
				return null;
		}
		
	}
	
	
	
	private static ScjdElementStyle[] singleStyle(Block block, EnumDyeColor color, int height) {
		return new ScjdElementStyle[] {new ScjdElementStyle(block, color, height)};
	}
	
	
	
	private static ScjdElementStyle[] singleStyle(Block block, int height) {
		return new ScjdElementStyle[] {new ScjdElementStyle(block, height)};
	}
	
	
	
	private static ScjdElementStyle[] doubleStyle(Block block1, int height1, Block block2, int height2) {
		return new ScjdElementStyle[] {new ScjdElementStyle(block1, height1), new ScjdElementStyle(block2, height2)};
	}
	
	
	
	public static class ScjdElementStyle {
		
		public final IBlockState state;
		public final int y;
		
		public ScjdElementStyle(Block block, int height) {
			this.state = block == null ? null : block.getDefaultState(); this.y = height;
		}
		
		public ScjdElementStyle(Block block, EnumDyeColor color, int height) {
			this.state = block.getDefaultState().withProperty(BlockColored.COLOR, color); this.y = height;
		}
		
	}
	
}
