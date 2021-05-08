package com.mndk.shapefile.shp;

import java.io.IOException;
import java.io.InputStream;

import com.mndk.shapefile.util.Endian;

public class ShapeVector {
	
	public final double x, y;
	
	public ShapeVector(double x, double y) {
		this.x = x; this.y = y;
	}
	
	public ShapeVector(InputStream is) throws IOException {
		this.x = Endian.readDoubleLittle(is);
		this.y = Endian.readDoubleLittle(is);
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}
	
}
