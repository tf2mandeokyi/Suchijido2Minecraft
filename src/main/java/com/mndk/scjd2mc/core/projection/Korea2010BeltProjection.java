package com.mndk.scjd2mc.core.projection;

public class Korea2010BeltProjection extends Proj4jProjection {

	private final String part;
	private final double lon_0;

	public Korea2010BeltProjection(String part, String crsName, double lon_0) {
		super(crsName, new String[]{
				"+proj=tmerc", "+lat_0=38", "+lon_0=" + lon_0, "+k=1", "+x_0=200000", "+y_0=600000", "+ellps=GRS80", "+units=m", "+no_defs"
		});
		this.part = part;
		this.lon_0 = lon_0;
	}

	@Override
	public String toWellKnownText() {
		return "PROJCS[\"Korea 2000 / " + this.part + " Belt 2010\",\n" +
				"    GEOGCS[\"Korea 2000\",\n" +
				"        DATUM[\"Geocentric_datum_of_Korea\",\n" +
				"            SPHEROID[\"GRS 1980\",6378137,298.257222101],\n" +
				"            TOWGS84[0,0,0,0,0,0,0]\n" +
				"        ],\n" +
				"        PRIMEM[\"Greenwich\",0],\n" +
				"        UNIT[\"degree\",0.0174532925199433]\n" +
				"    ],\n" +
				"    PROJECTION[\"Transverse_Mercator\"],\n" +
				"    PARAMETER[\"latitude_of_origin\",38],\n" +
				"    PARAMETER[\"central_meridian\"," + this.lon_0 + "],\n" +
				"    PARAMETER[\"scale_factor\",1],\n" +
				"    PARAMETER[\"false_easting\",200000],\n" +
				"    PARAMETER[\"false_northing\",600000],\n" +
				"    UNIT[\"metre\",1],\n" +
				"    AUTHORITY[\"EPSG\",\"" + this.crsName.replace("EPSG:", "") + "\"]\n" +
				"]";
	}
}
