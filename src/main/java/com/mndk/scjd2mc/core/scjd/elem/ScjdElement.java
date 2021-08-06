package com.mndk.scjd2mc.core.scjd.elem;

import com.google.gson.JsonObject;
import com.mndk.scjd2mc.core.scjd.geometry.GeometryShape;
import com.mndk.scjd2mc.core.scjd.geometry.MultiPolygon;
import com.mndk.scjd2mc.core.scjd.geometry.Polygon;
import com.mndk.scjd2mc.core.scjd.type.ElementStyleSelector;
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
public class ScjdElement<T extends GeometryShape<?>> {
	
	
	protected final ScjdLayer parent;
	protected final String id;
	@Getter protected T shape;
	public final Object[] dataRow;
	
	
	public Object getData(String key) {
		int index = parent.getDataColumnIndex(key);
		if(index == -1) return null;
		return dataRow[index];
	}
	
	
	public ScjdLayer getParent() { return parent; }



	public void generateBlocks(FlatRegion region, World world, TriangleList triangles) {
		ElementStyleSelector.ScjdElementStyle[] styles = ElementStyleSelector.getStyle(this);
		if(styles == null) return;
		this.shape.generateBlocks(styles, region, world, triangles);
	}



	private Map<String, Object> toSerializableMap() {

		BiConsumer<ScjdElement<?>, Map<String, Object>> jsonPropertyFunction =
				this.parent.getType().getSerializableMapPropertyFunction();
		if(jsonPropertyFunction == null) return null;

		Map<String, Object> result = new HashMap<>();

		result.put("type", "Feature");

		Map<String, Object> geometry = new HashMap<>();
		geometry.put("type", this.shape.getType().getName());
		geometry.put("coordinates", this.shape.toSerializableCoordinates());
		result.put("geometry", geometry);

		Map<String, Object> properties = new HashMap<>();
		if(this.shape instanceof Polygon || this.shape instanceof MultiPolygon) {
			properties.put("area", "yes");
		}
		jsonPropertyFunction.accept(this, properties);
		result.put("properties", properties);

		result.put("id", this.id);

		result.put("bounds", this.shape.getBoundingBox().toSerializableMap());

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
