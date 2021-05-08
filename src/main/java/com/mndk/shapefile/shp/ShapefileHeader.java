package com.mndk.shapefile.shp;

import java.io.IOException;
import java.io.InputStream;

import com.mndk.shapefile.util.Endian;

public class ShapefileHeader {
	
	public int fileLength;
	public int version;
	public ShapeType type;
	public BoundingBoxXYZM boundingBox;
	
	public ShapefileHeader(InputStream is) throws IOException {
		
		int fileCode = Endian.readIntegerBig(is);
		if(fileCode != 0x0000270a) {
			throw new IOException("File code does not match 0x0000270a");
		}
		
		/*int unused1 = */ Endian.readIntegerBig(is);
		/*int unused2 = */ Endian.readIntegerBig(is);
		/*int unused3 = */ Endian.readIntegerBig(is);
		/*int unused4 = */ Endian.readIntegerBig(is);
		/*int unused5 = */ Endian.readIntegerBig(is);
		
		this.fileLength = Endian.readIntegerBig(is);
		this.version = Endian.readIntegerLittle(is);
		this.type = ShapeType.getType(Endian.readIntegerLittle(is));
		this.boundingBox = new BoundingBoxXYZM(is);
		
	}
	
	@Override
	public String toString() {
		return "fileLength: " + fileLength + ", version: " + version + ", type: " + type + ", boundingBox: " + boundingBox;
	}
	
}
