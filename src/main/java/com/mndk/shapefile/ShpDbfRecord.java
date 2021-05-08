package com.mndk.shapefile;

import com.mndk.shapefile.dbf.DBaseRecord;
import com.mndk.shapefile.shp.ShapefileRecord;

public class ShpDbfRecord {

	public final ShapefileRecord shapeRecord;
	public final DBaseRecord dBaseRecord;
	
	public ShpDbfRecord(ShapefileRecord shapeRecord, DBaseRecord dBaseRecord) {
		this.shapeRecord = shapeRecord;
		this.dBaseRecord = dBaseRecord;
	}
	
	public ShapefileRecord getShape() {
		return shapeRecord;
	}
	
}
