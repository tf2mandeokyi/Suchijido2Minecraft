package com.mndk.scjd2mc.core.projection;

public class Korea2010BeltProjection extends Proj4jProjection {
	public Korea2010BeltProjection(String crsName, double lon_0) {
		super(crsName, new String[]{
				"+proj=tmerc", "+lat_0=38", "+lon_0=" + lon_0, "+k=1", "+x_0=200000", "+y_0=600000", "+ellps=GRS80", "+units=m", "+no_defs"
		});
	}

	public static class West extends Korea2010BeltProjection {
		public West() { super("EPSG:5185", 125); }
	}

	public static class Central extends Korea2010BeltProjection {
		public Central() { super("EPSG:5186", 127); }
	}

	public static class East extends Korea2010BeltProjection {
		public East() { super("EPSG:5187", 129); }
	}

	public static class EastSea extends Korea2010BeltProjection {
		public EastSea() { super("EPSG:5188", 131); }
	}
}
