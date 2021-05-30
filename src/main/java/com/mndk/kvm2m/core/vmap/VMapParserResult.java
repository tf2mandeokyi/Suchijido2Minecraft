package com.mndk.kvm2m.core.vmap;

import com.mndk.kvm2m.core.vmap.elem.VMapElementLayer;
import com.mndk.kvm2m.core.vmap.elem.poly.VMapPolygon;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VMapParserResult {
	
	private VMapPolygon boundary;
	private final List<VMapElementLayer> layerList;
	
	public VMapParserResult() {
		this.layerList = new ArrayList<>();
	}
	
	public VMapPolygon getBoundary() {
		return boundary;
	}
	
	public void setBoundary(VMapPolygon boundary) {
		this.boundary = boundary;
	}
	
	public List<VMapElementLayer> getElementLayers() {
		return this.layerList;
	}
	
	public void addElement(VMapElementLayer elementLayer) {
		this.layerList.add(elementLayer);
	}
	
	public void append(VMapParserResult other) {
		if(this.boundary == null) {
			this.boundary = other.boundary;
		}
		else {
			this.boundary = this.boundary.merge(other.boundary);
		}
		for(VMapElementLayer layer : other.layerList) {
			VMapElementLayer thisLayer = this.getLayer(layer.getType());
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
	public VMapElementLayer getLayer(VMapElementType type) {
		for(VMapElementLayer layer : this.layerList) {
			if(layer.getType() == type) return layer;
		}
		return null;
	}
	
	public boolean containsLayer(VMapElementType type) {
		return getLayer(type) != null;
	}
}