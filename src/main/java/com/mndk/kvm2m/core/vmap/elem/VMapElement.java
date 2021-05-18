package com.mndk.kvm2m.core.vmap.elem;

import java.util.Map;

import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.sk89q.worldedit.regions.FlatRegion;

import net.minecraft.world.World;

public abstract class VMapElement {
	
	
	protected final VMapElementLayer parent;
	protected final Object[] dataRow;
	
	
	protected VMapElement(VMapElementLayer parent, Object[] dataRow) {
		this.parent = parent;
		this.dataRow = dataRow;
	}
	
	
	public VMapElement(VMapElementLayer parent, Map<String, Object> dataRow) {
		this(parent, new Object[dataRow.size()]);
		for(Map.Entry<String, Object> data : dataRow.entrySet()) {
			int columnIndex = parent.getDataColumnIndex(data.getKey());
			if(columnIndex == -1) continue;
			this.dataRow[columnIndex] = data.getValue();
		}
	}
	
	
	public Object getDataByColumn(String columnName) {
		int index = parent.getDataColumnIndex(columnName);
		if(index == -1) return null;
		return dataRow[index];
	}
	
	
	public VMapElementLayer getParent() { return parent; }
	
	
	public abstract void generateBlocks(FlatRegion region, World world, TriangleList triangles);
	
	
	@Override
	public String toString() {
		return "VMapElement{type=" + parent.getType() + "}";
	}
}
