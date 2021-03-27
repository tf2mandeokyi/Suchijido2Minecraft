package com.mndk.kvm2m.core.vectormap;

import java.util.ArrayList;
import java.util.List;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vectormap.elem.VMapElement;
import com.mndk.kvm2m.core.vectormap.elem.poly.VMapPolyline;

public class VMapParserResult {
	
	private VMapPolyline boundary;
	private List<VMapElement> elementList;
	private List<Vector2DH> elevationPointList;
	
	public VMapParserResult() {
		this.elementList = new ArrayList<>();
		this.elevationPointList = new ArrayList<>();
	}
	
	public VMapPolyline getBoundary() {
		return boundary;
	}
	
	public void setBoundary(VMapPolyline boundary) {
		this.boundary = boundary;
	}
	
	public List<VMapElement> getElements() {
		return this.elementList;
	}
	
	public void addElement(VMapElement element) {
		this.elementList.add(element);
	}
	
	public List<Vector2DH> getElevationPoints() {
		return elevationPointList;
	}
}