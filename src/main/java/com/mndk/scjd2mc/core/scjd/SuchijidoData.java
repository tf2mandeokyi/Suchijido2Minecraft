package com.mndk.scjd2mc.core.scjd;

import com.mndk.scjd2mc.core.scjd.elem.ScjdLayer;
import com.mndk.scjd2mc.core.scjd.type.ElementDataType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SuchijidoData {

	private final List<ScjdLayer> layerList;
	
	public SuchijidoData() {
		this.layerList = new ArrayList<>();
	}
	
	public List<ScjdLayer> getLayers() {
		return this.layerList;
	}
	
	public void addLayer(ScjdLayer elementLayer) {
		this.layerList.add(elementLayer);
	}

	public void append(SuchijidoData other) {
		for(ScjdLayer layer : other.layerList) {
			this.getLayer(layer.getType()).addAll(layer);
		}
		Collections.sort(this.layerList);
	}

	@Nonnull
	public ScjdLayer getLayer(ElementDataType type) {
		for(ScjdLayer layer : this.layerList) {
			if(layer.getType() == type) return layer;
		}
		ScjdLayer result = new ScjdLayer(type);
		this.layerList.add(result);
		return result;
	}
}