package com.mndk.kvm2m.core.vmap.elem;

import com.google.gson.JsonObject;
import com.mndk.kvm2m.core.util.shape.BoundingBoxDouble;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vmap.elem.poly.VMapPolygon;
import com.mndk.kvm2m.core.vmap.type.VMapElementGeomType;
import com.sk89q.worldedit.regions.FlatRegion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.world.World;

import java.util.Map;
import java.util.function.BiConsumer;

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
	
	
	public Object getData(String key) {
		int index = parent.getDataColumnIndex(key);
		if(index == -1) return null;
		return dataRow[index];
	}
	
	
	public VMapLayer getParent() { return parent; }
	
	
	public abstract void generateBlocks(FlatRegion region, World world, TriangleList triangles);
	public BoundingBoxDouble getBoundingBoxDouble() {
		return bbox;
	}


	protected abstract JsonObject getJsonGeometryData();
	public JsonObject toJsonObject() {
		JsonObject result = new JsonObject();

		result.addProperty("type", "Feature");

		result.add("geometry", this.getJsonGeometryData());

		JsonObject properties = new JsonObject();
		if(this instanceof VMapPolygon) {
			properties.addProperty("area", "yes");
		}

		BiConsumer<VMapElement, JsonObject> jsonPropertyFunction = this.parent.getType().getJsonPropertyFunction();
		if(jsonPropertyFunction != null) {
			jsonPropertyFunction.accept(this, properties);
		}
		result.add("properties", properties);

		return result;
	}


	@Override
	public String toString() {
		return "VMapElement{type=" + parent.getType() + "}";
	}
}
