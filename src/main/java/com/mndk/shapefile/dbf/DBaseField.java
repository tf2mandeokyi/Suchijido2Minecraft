package com.mndk.shapefile.dbf;

import java.io.IOException;

import com.mndk.shapefile.util.ShapefileCustomInputStream;

public class DBaseField {

	
	public final String name;
	public final DBaseFieldType type;
	public final byte length;
	public final byte decimalCount;
	public final short workAreaId;
	public final byte example;
	public final boolean productionMdxFieldFlag;
	
	
	
	public DBaseField(ShapefileCustomInputStream is) throws IOException {
		
		this.name = is.readString(11).trim();
		this.type = DBaseFieldType.from((byte) is.read());
		
		/*int reserved0 = */ is.readIntLittle();
		
		this.length = (byte) is.read();
		this.decimalCount = (byte) is.read();
		
		this.workAreaId = is.readShortLittle(); // TODO figure out whether this is little or big
		
		this.example = (byte) is.read();
		
		/*uint10 reserved1 = */ is.readLongLittle(); is.readShortLittle();
		
		this.productionMdxFieldFlag = is.read() == 1;
		
	}
	
	
	
	@Override
	public String toString() {
		return "DBaseField{name=" + name + ", type=" + type + ", length=" + length + ", ...}";
	}
	
}
