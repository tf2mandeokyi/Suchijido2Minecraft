package com.mndk.kvm2m.core.vectormap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vectormap.elem.VMapElement;
import com.mndk.kvm2m.core.vectormap.elem.VMapElementLayer;
import com.mndk.kvm2m.core.vectormap.elem.poly.VMapPolyline;

public class VMapParserResult {
	
	private VMapPolyline boundary;
	private List<VMapElementLayer> layerList;
	private List<Vector2DH> elevationPointList;
	
	public VMapParserResult() {
		this.layerList = new ArrayList<>();
		this.elevationPointList = new ArrayList<>();
	}
	
	public VMapPolyline getBoundary() {
		return boundary;
	}
	
	public void setBoundary(VMapPolyline boundary) {
		this.boundary = boundary;
	}
	
	public List<VMapElementLayer> getElementLayers() {
		return this.layerList;
	}
	
	public void addElement(VMapElementLayer elementLayer) {
		this.layerList.add(elementLayer);
	}
	
	public List<Vector2DH> getElevationPoints() {
		return elevationPointList;
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
				for(VMapElement element : layer) {
					thisLayer.add(element);
				}
			}
			else {
				this.layerList.add(layer);
			}
		}
		this.elevationPointList.addAll(other.elevationPointList);
		Collections.sort(this.layerList, (l1, l2) -> {
			if(l1.getType().ordinal() > l2.getType().ordinal()) return 1;
			else if(l1.getType().ordinal() < l2.getType().ordinal()) return -1;
			return 0;
		});
	}
	
	public VMapElementLayer getLayer(VMapElementType type) {
		for(VMapElementLayer layer : this.layerList) {
			if(layer.getType() == type) return layer;
		}
		return null;
	}
}