package com.mndk.kvm2m.core.vectormap;

import java.util.ArrayList;
import java.util.List;

import com.mndk.kvm2m.core.util.math.Vector2DH;
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
		this.layerList.addAll(other.layerList);
		this.elevationPointList.addAll(other.elevationPointList);
	}
}