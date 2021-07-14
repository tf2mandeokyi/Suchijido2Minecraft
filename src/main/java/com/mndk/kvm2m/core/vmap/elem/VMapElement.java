package com.mndk.kvm2m.core.vmap.elem;

import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vmap.VMapElementGeomType;
import com.sk89q.worldedit.regions.FlatRegion;
import net.minecraft.world.World;

import java.util.Map;

public abstract class VMapElement {
	
	
	protected final VMapLayer parent;
	protected final Object[] dataRow;
	protected VMapElementGeomType type;
	
	
	protected VMapElement(VMapLayer parent, Object[] dataRow, VMapElementGeomType type) {
		this.parent = parent;
		this.dataRow = dataRow;
		this.type = type;
	}
	
	
	public VMapElement(VMapLayer parent, Map<String, Object> dataRow, VMapElementGeomType type) {
		this(parent, new Object[dataRow.size()], type);
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
	
	
	public VMapLayer getParent() { return parent; }
	
	
	public abstract void generateBlocks(FlatRegion region, World world, TriangleList triangles);
	
	
	@Override
	public String toString() {
		return "VMapElement{type=" + parent.getType() + "}";
	}
}
