package com.mndk.kvm2m.core.vectormap;

import java.util.ArrayList;
import java.util.List;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vectormap.elem.point.VectorMapPoint;
import com.mndk.kvm2m.core.vectormap.elem.poly.VectorMapPolyline;

public class VectorMapParserResult {
	
	private VectorMapPolyline boundary;
	private List<VectorMapPolyline> polylineList;
	private List<VectorMapPoint> pointList;
	private List<Vector2DH> elevationPointList;
	
	VectorMapParserResult() {
		this.polylineList = new ArrayList<>();
		this.pointList = new ArrayList<>();
		this.elevationPointList = new ArrayList<>();
	}
	public VectorMapPolyline getBoundary() {
		return boundary;
	}
	public void setBoundary(VectorMapPolyline boundary) {
		this.boundary = boundary;
	}
	public List<VectorMapPolyline> getPolylines() {
		return polylineList;
	}
	public List<VectorMapPoint> getPoints() {
		return pointList;
	}
	public List<Vector2DH> getElevationPoints() {
		return elevationPointList;
	}
}