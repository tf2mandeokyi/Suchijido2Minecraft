package com.mndk.scjd2mc.core.scjd;

import com.mndk.scjd2mc.core.scjd.elem.ScjdLayer;
import com.mndk.scjd2mc.core.scjd.elem.poly.ScjdPolygon;
import com.mndk.scjd2mc.core.scjd.type.ElementDataType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScjdReaderResult {
	
	private ScjdPolygon boundary;
	private final List<ScjdLayer> layerList;
	
	public ScjdReaderResult() {
		this.layerList = new ArrayList<>();
	}
	
	public ScjdPolygon getBoundary() {
		return boundary;
	}
	
	public void setBoundary(ScjdPolygon boundary) {
		this.boundary = boundary;
	}
	
	public List<ScjdLayer> getLayers() {
		return this.layerList;
	}
	
	public void addLayer(ScjdLayer elementLayer) {
		this.layerList.add(elementLayer);
	}
	
	public void append(ScjdReaderResult other) {
		if(this.boundary == null) {
			this.boundary = other.boundary;
		}
		else {
			this.boundary = this.boundary.merge(other.boundary);
		}
		for(ScjdLayer layer : other.layerList) {
			ScjdLayer thisLayer = this.getLayer(layer.getType());
			if(thisLayer != null) {
				thisLayer.addAll(layer);
			}
			else {
				this.layerList.add(layer);
			}
		}
		
		Collections.sort(this.layerList);
	}

	@Nullable
	public ScjdLayer getLayer(ElementDataType type) {
		for(ScjdLayer layer : this.layerList) {
			if(layer.getType() == type) return layer;
		}
		return null;
	}
	
	public boolean containsLayer(ElementDataType type) {
		return getLayer(type) != null;
	}
}