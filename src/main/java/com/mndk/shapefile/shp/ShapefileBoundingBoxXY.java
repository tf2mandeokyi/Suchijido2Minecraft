package com.mndk.shapefile.shp;

import java.io.IOException;

import com.mndk.shapefile.util.ShapefileCustomInputStream;

public class ShapefileBoundingBoxXY {

	public final double xmin, ymin, xmax, ymax;
	
	public ShapefileBoundingBoxXY(ShapefileCustomInputStream is) throws IOException {
		this.xmin = is.readDoubleLittle(); this.ymin = is.readDoubleLittle();
		this.xmax = is.readDoubleLittle(); this.ymax = is.readDoubleLittle();
	}
	
	public ShapefileBoundingBoxXY(double xmin, double ymin, double xmax, double ymax) {
		this.xmin = xmin; this.ymin = ymin;
		this.xmax = xmax; this.ymax = ymax;
	}
	
	@Override
	public String toString() {
		return "x: [" + xmin + ", " + xmax + "], y: [" + ymin + ", " + ymax + "]";
	}
	
}