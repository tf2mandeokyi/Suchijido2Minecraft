package com.mndk.kvm2m.core.vmap;

import com.mndk.kvm2m.core.vmap.elem.VMapElement;
import com.mndk.kvm2m.core.vmap.elem.poly.VMapBuilding;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.EnumDyeColor;

import java.lang.reflect.Field;

public class VMapElementStyleSelector {
	
	
	
	public static VMapElementStyle[] getStyle(VMapElement element) {
		
		
		
		int height;
		
		switch(element.getParent().getType()) {
			// A types
			case 도로경계: 
				return singleStyle(get("CONCRETE"), EnumDyeColor.GRAY, 0);
			case 입체교차부:
				return singleStyle(get("CONCRETE"), EnumDyeColor.SILVER, 3);
			case 보도: 
			case 안전지대:
				if("어린이보호구역".equals(element.getDataByColumn("구조"))) return null;
				return singleStyle(get("DOUBLE_STONE_SLAB"), 0);
			case 승강장:
				return singleStyle(get("DOUBLE_STONE_SLAB"), 1);
			case 횡단보도: 
				return singleStyle(get("WOOL"), EnumDyeColor.WHITE, 0);
			case 육교:
				return singleStyle(get("IRON_BLOCK"), 3);
			case 교량:
				return singleStyle(get("IRON_BLOCK"), 0);
			case 승강장_지붕:
				return singleStyle(get("WOOL"), EnumDyeColor.SILVER, 2);
			case 터널: 
				return singleStyle(get("CONCRETE"), EnumDyeColor.MAGENTA, 0);
			case 철도:
				return singleStyle(get("ANVIL"), 1);
			
			// B types
			case 건물:
				if(element instanceof VMapBuilding) {
					Object floorCount = element.getDataByColumn("층수");
					if(floorCount instanceof Integer) height = (Integer) element.getDataByColumn("층수") * VMapBuilding.FLOOR_HEIGHT;
					else height = (int) Math.round((Double) element.getDataByColumn("층수")) * VMapBuilding.FLOOR_HEIGHT;
					return singleStyle(get("STONE"), height);
				}
				else return singleStyle(get("BRICK_BLOCK"), 0);
			case 담장:
				return singleStyle(get("OAK_FENCE"), 1);
			
			// C types
			case 조명:
				return singleStyle(get("REDSTONE_LAMP"), 1);
			case 신호등:
				return singleStyle(get("NETHER_BRICK_FENCE"), 1);
			case 도로분리대:
				return singleStyle(get("CONCRETE"), 1);
				
			// D types
			case 독립수:
				return singleStyle(get("LOG"), 1);
				
			// E types
			/*case 하천중심선:
			case 등심선: 
				return singleStyle("LAPIS_BLOCK", 0);*/
			case 실폭하천:
			case 호수:
			case 해안선:
				return doubleStyle(get("AIR"), 0, get("WATER"), -1);
				
			// F types
			case 등고선:
				return singleStyle(get("DIAMOND_BLOCK"), (int) Math.round((Double) element.getDataByColumn("등고수치")));
			case 표고점:
				return singleStyle(get("DIAMOND_BLOCK"), (int) Math.round((Double) element.getDataByColumn("수치")));
			case 절토:
				return singleStyle(get("END_BRICKS"), 1);
			case 옹벽:
				height = (int) Math.round((Double) element.getDataByColumn("높이"));
				return singleStyle(get("END_BRICKS"), height);
				
			// G types
			case 기타경계:
				if("기타콘크리트구조물".equals(element.getDataByColumn("용도")))
					return singleStyle(get("CONCRETE"), 0);
					
			
			default:
				return null;
		}
		
	}
	
	
	
	private static VMapElementStyle[] singleStyle(Block block, EnumDyeColor color, int height) {
		return new VMapElementStyle[] {new VMapElementStyle(block, color, height)};
	}
	
	
	
	private static VMapElementStyle[] singleStyle(Block block, int height) {
		return new VMapElementStyle[] {new VMapElementStyle(block, height)};
	}
	
	
	
	private static VMapElementStyle[] doubleStyle(Block block1, int height1, Block block2, int height2) {
		return new VMapElementStyle[] {new VMapElementStyle(block1, height1), new VMapElementStyle(block2, height2)};
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
	
	
	private static Block get(String name) {
		if(!Bootstrap.isRegistered()) {
			return null;
		}
		try {
			Field f = Blocks.class.getField(name);
			Class<?> t = f.getType();
			if (t == Block.class) {
				return (Block) f.get(null);
			}
		} catch(NoSuchFieldException | IllegalAccessException ignored) {}
		return null;
	}
	
}
