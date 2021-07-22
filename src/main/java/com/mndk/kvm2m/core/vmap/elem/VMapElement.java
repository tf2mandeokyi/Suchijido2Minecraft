package com.mndk.kvm2m.core.vmap.elem;

import com.mndk.kvm2m.core.util.shape.BoundingBoxDouble;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vmap.VMapElementGeomType;
import com.sk89q.worldedit.regions.FlatRegion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.world.World;

import java.util.Map;

@AllArgsConstructor
public abstract class VMapElement {
	
	
	protected final VMapLayer parent;
	public final Object[] dataRow;
	@Getter protected VMapElementGeomType geometryType;
	protected BoundingBoxDouble bbox;


	protected VMapElement(VMapLayer parent, Object[] dataRow, VMapElementGeomType geometryType) {
		this.parent = parent;
		this.dataRow = dataRow;
		this.geometryType = geometryType;
	}
	
	
	public VMapElement(VMapLayer parent, Map<String, Object> dataRow, VMapElementGeomType geometryType) {
		this(parent, new Object[dataRow.size()], geometryType);
		for(Map.Entry<String, Object> data : dataRow.entrySet()) {
			int columnIndex = parent.getDataColumnIndex(data.getKey());
			if(columnIndex == -1) continue;
			this.dataRow[columnIndex] = data.getValue();
		}
	}
	
	
	public Object getData(String columnName) {
		int index = parent.getDataColumnIndex(columnName);
		if(index == -1) return null;
		return dataRow[index];
	}
	
	
	public VMapLayer getParent() { return parent; }
	
	
	public abstract void generateBlocks(FlatRegion region, World world, TriangleList triangles);
	public BoundingBoxDouble getBoundingBoxDouble() {
		return bbox;
	}
	
	
	@Override
	public String toString() {
		return "VMapElement{type=" + parent.getType() + "}";
	}
}
