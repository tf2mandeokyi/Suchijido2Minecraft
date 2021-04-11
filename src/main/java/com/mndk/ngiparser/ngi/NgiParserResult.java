package com.mndk.ngiparser.ngi;

import java.util.Map;

import com.mndk.ngiparser.ngi.element.NgiRecord;

public class NgiParserResult {

	private Map<Integer, NgiLayer> layers;
	private Map<Integer, NgiRecord<?>> elements;
	
	public NgiParserResult(Map<Integer, NgiLayer> layers, Map<Integer, NgiRecord<?>> elements) {
		this.layers = layers; this.elements = elements;
	}
	
	public NgiLayer getLayer(int id) {
		return this.layers.get(id);
	}
	
	public Map<Integer, NgiLayer> getLayers() {
		return this.layers;
	}
	
	public void addLayer(int id, NgiLayer layer) {
		this.layers.put(id, layer);
	}
	
	public NgiRecord<?> getElement(int id) {
		return this.elements.get(id);
	}
	
	public Map<Integer, NgiRecord<?>> getElements() {
		return this.elements;
	}

	public void addElement(int id, NgiRecord<?> layer) {
		this.elements.put(id, layer);
	}
	
}
