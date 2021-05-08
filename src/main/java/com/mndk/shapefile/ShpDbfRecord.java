package com.mndk.shapefile;

import com.mndk.shapefile.dbf.DBaseRecord;
import com.mndk.shapefile.shp.ShapefileRecord;

public class ShpDbfRecord {

	public final ShapefileRecord shape;
	public final DBaseRecord dBase;
	
	public ShpDbfRecord(ShapefileRecord shape, DBaseRecord dBase) {
		this.shape = shape;
		this.dBase = dBase;
	}
	
	@Override
	public String toString() {
		return "ShpDbfRecord{"
				+ "\n  Shape=" + shape
				+ "\n  Data=" + dBase
				+ "\n}";
	}
	
}
