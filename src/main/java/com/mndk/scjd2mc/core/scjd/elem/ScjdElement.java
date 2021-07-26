package com.mndk.scjd2mc.core.scjd.elem;

import com.google.gson.JsonObject;
import com.mndk.scjd2mc.core.scjd.elem.poly.ScjdPolygon;
import com.mndk.scjd2mc.core.scjd.type.ElementGeometryType;
import com.mndk.scjd2mc.core.util.shape.BoundingBoxDouble;
import com.mndk.scjd2mc.core.util.shape.TriangleList;
import com.mndk.scjd2mc.mod.Suchijido2MinecraftMod;
import com.sk89q.worldedit.regions.FlatRegion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.world.World;

import java.util.HashMap;
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


	protected abstract Map<String, Object> getSerializableMapGeometryData();

	private Map<String, Object> toSerializableMap() {

		BiConsumer<ScjdElement, Map<String, Object>> jsonPropertyFunction = this.parent.getType().getSerializableMapPropertyFunction();
		if(jsonPropertyFunction == null) return null;

		Map<String, Object> result = new HashMap<>();

		result.put("type", "Feature");

		result.put("geometry", this.getSerializableMapGeometryData());

		Map<String, Object> properties = new HashMap<>();
		if(this instanceof ScjdPolygon) {
			properties.put("area", "yes");
		}
		jsonPropertyFunction.accept(this, properties);
		result.put("properties", properties);

		result.put("id", this.id);

		result.put("bounds", this.bbox.toSerializableMap());

		return result;
	}

	public JsonObject toJsonObject() {
		Map<String, Object> result = this.toSerializableMap();
		return result == null ? null : Suchijido2MinecraftMod.gson.toJsonTree(this.toSerializableMap()).getAsJsonObject();
	}

	public org.bson.Document toBsonDocument(String map_index) {
		Map<String, Object> map = this.toSerializableMap();
		if(map == null) return null;
		map.remove("id");
		map.put("map_index", map_index);
		return new org.bson.Document(map);
	}


	@Override
	public String toString() {
		return "VMapElement{type=" + parent.getType() + "}";
	}
}
