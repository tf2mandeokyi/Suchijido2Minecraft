package com.mndk.scjd2mc.core.scjd.elem;

import com.mndk.scjd2mc.core.scjd.geometry.LineString;

public class ScjdCoastline extends ScjdContour {

	public ScjdCoastline(ScjdLayer parent, LineString lineString, Object[] rowData) {
		super(parent, lineString, rowData, 0);
	}

	@Override
	public String toString() {
		return "VMapCoastline{vertexLen=" + shape.size() + "}";
	}
}
