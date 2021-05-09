package com.mndk.kvm2m.core.vmap.elem.line;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class VMapContourList extends ArrayList<VMapContour> {
	
	private final int elevation;
	
	public VMapContourList(int elevation) {
		super();
		this.elevation = elevation;
	}
	
	public VMapContourList(VMapContour initial) {
		this(initial.elevation);
		add(initial);
	}
	
	@Override
	public boolean add(VMapContour e) {
		if(e.elevation != this.elevation) {
			throw new RuntimeException("Elevation data does not match");
		}
		return super.add(e);
	}
	
	public int getElevation() {
		return elevation;
	}
	
}