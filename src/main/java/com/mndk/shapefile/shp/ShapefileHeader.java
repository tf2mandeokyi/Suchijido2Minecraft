package com.mndk.shapefile.shp;

import java.io.IOException;

import com.mndk.shapefile.util.ShapefileCustomInputStream;

public class ShapefileHeader {
	
	public int fileLength;
	public int version;
	public ShapeType type;
	public ShapefileBoundingBoxXYZM boundingBox;
	
	public ShapefileHeader(ShapefileCustomInputStream is) throws IOException {
		
		int fileCode = is.readIntBig();
		if(fileCode != 0x0000270a) {
			throw new IOException("File code does not match 0x0000270a");
		}
		
		/*int unused1 = */ is.readIntBig();
		/*int unused2 = */ is.readIntBig();
		/*int unused3 = */ is.readIntBig();
		/*int unused4 = */ is.readIntBig();
		/*int unused5 = */ is.readIntBig();
		
		this.fileLength = is.readIntBig();
		this.version = is.readIntLittle();
		this.type = ShapeType.getType(is.readIntLittle());
		this.boundingBox = new ShapefileBoundingBoxXYZM(is);
		
	}
	
	@Override
	public String toString() {
		return "fileLength: " + fileLength + ", version: " + version + ", type: " + type + ", boundingBox: " + boundingBox;
	}
	
}
