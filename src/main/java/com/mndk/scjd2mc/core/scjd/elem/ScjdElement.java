package com.mndk.scjd2mc.core.scjd.elem;

import com.google.gson.JsonObject;
import com.mndk.scjd2mc.core.util.shape.BoundingBoxDouble;
import com.mndk.scjd2mc.core.util.shape.TriangleList;
import com.mndk.scjd2mc.core.scjd.elem.poly.ScjdPolygon;
import com.mndk.scjd2mc.core.scjd.type.ElementGeometryType;
import com.sk89q.worldedit.regions.FlatRegion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.world.World;

import java.util.Map;
import java.util.function.BiConsumer;

@AllArgsConstructor
public abstract class ScjdElement {
	
	
	protected final ScjdLayer parent;
	public final Object[] dataRow;
	@Getter protected ElementGeometryType geometryType;
	protected BoundingBoxDouble bbox;
	protected final String id;


	protected ScjdElement(ScjdLayer parent, String id, Object[] dataRow, ElementGeometryType geometryType) {
		this.parent = parent;
		this.dataRow = dataRow;
		this.geometryType = geometryType;
		this.id = id;
	}
	
	
	public ScjdElement(ScjdLayer parent, String id, Map<String, Object> dataRow, ElementGeometryType geometryType) {
		this(parent, id, new Object[dataRow.size()], geometryType);
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
	
	
	public ScjdLayer getParent() { return parent; }
	
	
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
		if(this instanceof ScjdPolygon) {
			properties.addProperty("area", "yes");
		}

		BiConsumer<ScjdElement, JsonObject> jsonPropertyFunction = this.parent.getType().getJsonPropertyFunction();
		if(jsonPropertyFunction != null) {
			jsonPropertyFunction.accept(this, properties);
		}
		result.add("properties", properties);

		result.addProperty("id", this.id);

		return result;
	}


	@Override
	public String toString() {
		return "VMapElement{type=" + parent.getType() + "}";
	}
}
