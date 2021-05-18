package com.mndk.kvm2m.core.vmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mndk.kvm2m.core.vmap.elem.VMapElement;
import com.mndk.kvm2m.core.vmap.elem.VMapElementLayer;
import com.mndk.kvm2m.core.vmap.elem.poly.VMapPolygon;

public class VMapParserResult {
	
	private VMapPolygon boundary;
	private List<VMapElementLayer> layerList;
	
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
				for(VMapElement element : layer) {
					thisLayer.add(element);
				}
			}
			else {
				this.layerList.add(layer);
			}
		}
		
		Collections.sort(this.layerList);
	}
	
	public VMapElementLayer getLayer(VMapElementType type) {
		for(VMapElementLayer layer : this.layerList) {
			if(layer.getType() == type) return layer;
		}
		return null;
	}
	
	public boolean containsLayer(VMapElementType type) {
		return getLayer(type) != null;
	}
	
	
	/*
	public void extractElevationPoints() {
		
		// Find all contour lines
		if(this.containsLayer(VMapElementType.등고선)) for(VMapElement element : this.getLayer(VMapElementType.등고선)) {
			if(!(element instanceof VMapContour)) continue;
			VMapContour contour = (VMapContour) element;
			for(Vector2DH[] va : contour.getVertexList()) for(Vector2DH v : va) {
				this.elevationPointList.add(v.withHeight(contour.elevation));
			}
		}
		
		// Find all elevation points, but filter out points which are on both roads and rivers
		if(this.containsLayer(VMapElementType.표고점)) for(VMapElement elevElement : this.getLayer(VMapElementType.표고점)) {
				
			if(!(elevElement instanceof VMapElevationPoint)) continue;
			VMapElevationPoint elevationPoint = (VMapElevationPoint) elevElement;
			boolean onRoad = false, onRiver = false;
			
			// Iterate all roads
			if(this.containsLayer(VMapElementType.도로경계)) for(VMapElement roadElement : this.getLayer(VMapElementType.도로경계)) {
				
				if(!(roadElement instanceof VMapPolygon)) continue;
				VMapPolygon road = (VMapPolygon) roadElement;
				
				// Exclude line-roads
				if(!road.shouldBeFilled()) continue;
				
				if(road.containsPoint(elevationPoint.toVector())) {
					onRoad = true; break;
				}
			}
			
			// Iterate all rivers
			if(this.containsLayer(VMapElementType.실폭하천)) for(VMapElement riverElement : this.getLayer(VMapElementType.실폭하천)) {
				
				if(!(riverElement instanceof VMapPolygon)) continue;
				VMapPolygon road = (VMapPolygon) riverElement;
				
				// Exclude line-roads
				if(!road.shouldBeFilled()) continue;
				
				if(road.containsPoint(elevationPoint.toVector())) {
					onRiver = true; break;
				}
			}
			
			if(!onRiver || !onRoad) this.elevationPointList.add(elevationPoint.toVector());
		}
	}
	*/
}