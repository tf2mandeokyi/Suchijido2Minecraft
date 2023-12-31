package com.mndk.scjdmc.ngiparser.ngi;

import com.mndk.scjdmc.ngiparser.ngi.element.NgiRecord;

import java.util.Map;

public record NgiParserResult(Map<Integer, NgiLayer> layers, Map<Integer, NgiRecord<?>> elements) {

	public NgiLayer getLayer(int id) {
		return this.layers.get(id);
	}

	public void addLayer(int id, NgiLayer layer) {
		this.layers.put(id, layer);
	}

	public NgiRecord<?> getElement(int id) {
		return this.elements.get(id);
	}

	public void addElement(int id, NgiRecord<?> layer) {
		this.elements.put(id, layer);
	}

}
