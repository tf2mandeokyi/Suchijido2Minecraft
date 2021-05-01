package com.mndk.shapefile.shp;

public class ShpBoundingBox {

	public final double xmin, ymin, zmin, mmin;
	public final double xmax, ymax, zmax, mmax;
	
	public ShpBoundingBox(double xmin, double ymin, double zmin, double mmin, double xmax, double ymax, double zmax, double mmax) {
		this.xmin = xmin; this.ymin = ymin; this.zmin = zmin; this.mmin = mmin;
		this.xmax = xmax; this.ymax = ymax; this.zmax = zmax; this.mmax = mmax;
	}
	
	public ShpBoundingBox(double xmin, double ymin, double xmax, double ymax) {
		this(xmin, ymin, 0, 0, xmax, ymax, 0, 0);
	}
	
}
