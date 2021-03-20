package com.mndk.kvm2m.core.vectormap;

import java.util.ArrayList;
import java.util.List;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vectormap.elem.VectorMapElement;
import com.mndk.kvm2m.core.vectormap.elem.poly.VectorMapPolyline;

public class VectorMapParserResult {
	
	private VectorMapPolyline boundary;
	private List<VectorMapElement> elementList;
	private List<Vector2DH> elevationPointList;
	
	public VectorMapParserResult() {
		this.elementList = new ArrayList<>();
		this.elevationPointList = new ArrayList<>();
	}
	
	public VectorMapPolyline getBoundary() {
		return boundary;
	}
	
	public void setBoundary(VectorMapPolyline boundary) {
		this.boundary = boundary;
	}
	
	public List<VectorMapElement> getElements() {
		return this.elementList;
	}
	
	public void addElement(VectorMapElement element) {
		this.elementList.add(element);
	}
	
	public List<Vector2DH> getElevationPoints() {
		return elevationPointList;
	}
}