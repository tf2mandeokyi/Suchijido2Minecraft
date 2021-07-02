package com.mndk.shapefile.shp;

public enum ShapeType {
	NULL(0),
	POINT(1),
	POLYLINE(3),
	POLYGON(5),
	MULTIPOINT(8),
	POINTZ(11),
	POLYLINEZ(13),
	POLYGONZ(15),
	MULTIPOINTZ(18),
	POINTM(21),
	POLYLINEM(23),
	POLYGONM(25),
	MULTIPOINTM(28),
	MULTIPATCH(31);
	
	private final int code;
	ShapeType(int code) {
		this.code = code;
	}
	
	public static ShapeType getType(int code) {
		for(ShapeType type : values()) {
			if(type.code == code) return type;
		}
		return null;
	}
}
