package com.mndk.shapefile.shp;

import java.io.IOException;
import java.io.InputStream;

import com.mndk.shapefile.util.Endian;

public class ShpBoundingBoxXYZM extends ShpBoundingBoxXY {

	public final double zmin, zmax, mmin, mmax;
	
	public ShpBoundingBoxXYZM(InputStream is) throws IOException {
		super(is);
		this.zmin = Endian.readDoubleLittle(is); this.zmax = Endian.readDoubleLittle(is);
		this.mmin = Endian.readDoubleLittle(is); this.mmax = Endian.readDoubleLittle(is);
	}
	
	public ShpBoundingBoxXYZM(double xmin, double ymin, double zmin, double mmin, double xmax, double ymax, double zmax, double mmax) {
		super(xmin, ymin, xmax, ymax);
		this.zmin = zmin; this.zmax = zmax; this.mmin = mmin; this.mmax = mmax;
	}
	
	@Override
	public String toString() {
		return "x: [" + xmin + ", " + xmax + "], y: [" + ymin + ", " + ymax + "], z: [" + zmin + ", " + zmax + "], m: [" + mmin + ", " + mmax + "]";
	}
	
}
