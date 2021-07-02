package com.mndk.shapefile.shp;

import com.mndk.shapefile.util.ShapefileCustomInputStream;

import java.io.IOException;

public class ShapeVector {
	
	public final double x, y;
	
	public ShapeVector(double x, double y) {
		this.x = x; this.y = y;
	}
	
	public ShapeVector(ShapefileCustomInputStream is) throws IOException {
		this.x = is.readDoubleLittle();
		this.y = is.readDoubleLittle();
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}
	
}
