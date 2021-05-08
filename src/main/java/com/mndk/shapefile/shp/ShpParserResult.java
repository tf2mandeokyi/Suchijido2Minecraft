package com.mndk.shapefile.shp;

import java.util.List;

public class ShpParserResult {

	public final ShpHeader header;
	public final List<ShpRecord> records;
	
	public ShpParserResult(ShpHeader header, List<ShpRecord> records) {
		this.header = header;
		this.records = records;
	}
	
}
