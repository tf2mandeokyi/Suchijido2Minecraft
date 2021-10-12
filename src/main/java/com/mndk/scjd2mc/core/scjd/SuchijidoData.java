package com.mndk.scjd2mc.core.scjd;

import com.mndk.scjd2mc.core.scjd.elem.ScjdElement;
import com.mndk.scjd2mc.core.scjd.elem.ScjdLayer;
import com.mndk.scjd2mc.core.scjd.type.ElementDataType;
import com.mndk.scjd2mc.core.util.shape.BoundingBoxDouble;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.*;

public class SuchijidoData {

	private final Map<ElementDataType, ScjdLayer> layerMap;
	@Getter protected BoundingBoxDouble boundingBox;
	
	public SuchijidoData() {
		this.layerMap = new HashMap<>();
		this.boundingBox = BoundingBoxDouble.ILLEGAL_INFINITE;
	}
	
	public List<ScjdLayer> getLayers() {
		List<ScjdLayer> result = new ArrayList<>();
		for(Map.Entry<ElementDataType, ScjdLayer> entry : this.layerMap.entrySet()) {
			result.add(entry.getValue());
		}
		Collections.sort(result);
		return result;
	}
	
	public void addLayer(ScjdLayer elementLayer) {
		ElementDataType type = elementLayer.getType();
		this.layerMap.putIfAbsent(type, new ScjdLayer(type));
		ScjdLayer originalLayer = this.layerMap.get(type);
		for(ScjdElement<?> element : elementLayer) {
			originalLayer.add(element);
			this.boundingBox = this.boundingBox.or(element.getShape().getBoundingBox());
		}
	}

	public final void append(SuchijidoData other) {
		for(Map.Entry<ElementDataType, ScjdLayer> entry : other.layerMap.entrySet()) {
			this.addLayer(entry.getValue());
		}
	}

	@Nonnull
	public ArrayList<ScjdElement<?>> getLayer(ElementDataType type) {
		this.layerMap.putIfAbsent(type, new ScjdLayer(type));
		return this.layerMap.get(type);
	}
}