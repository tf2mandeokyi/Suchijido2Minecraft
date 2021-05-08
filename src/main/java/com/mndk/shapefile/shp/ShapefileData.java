package com.mndk.shapefile.shp;

import java.util.List;

public class ShapefileData {

	public final ShapefileHeader header;
	public final List<ShapeRecord> records;
	
	public ShapefileData(ShapefileHeader header, List<ShapeRecord> records) {
		this.header = header;
		this.records = records;
	}
	
}
