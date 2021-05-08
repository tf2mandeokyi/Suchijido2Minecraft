package com.mndk.shapefile.shp;

import java.io.IOException;

import com.mndk.shapefile.util.ShapefileCustomInputStream;

public class ShapefileBoundingBoxXYZM extends ShapefileBoundingBoxXY {

	public final double zmin, zmax, mmin, mmax;
	
	public ShapefileBoundingBoxXYZM(ShapefileCustomInputStream is) throws IOException {
		super(is);
		this.zmin = is.readDoubleLittle(); this.zmax = is.readDoubleLittle();
		this.mmin = is.readDoubleLittle(); this.mmax = is.readDoubleLittle();
	}
	
	public ShapefileBoundingBoxXYZM(double xmin, double ymin, double zmin, double mmin, double xmax, double ymax, double zmax, double mmax) {
		super(xmin, ymin, xmax, ymax);
		this.zmin = zmin; this.zmax = zmax; this.mmin = mmin; this.mmax = mmax;
	}
	
	@Override
	public String toString() {
		return "x: [" + xmin + ", " + xmax + "], y: [" + ymin + ", " + ymax + "], z: [" + zmin + ", " + zmax + "], m: [" + mmin + ", " + mmax + "]";
	}
	
}
