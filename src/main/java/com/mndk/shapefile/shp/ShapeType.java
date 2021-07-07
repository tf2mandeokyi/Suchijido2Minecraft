package com.mndk.shapefile.shp;

public enum ShapeType {
	NULL(0, false, false),

	POINT(1, false, false),
	POINTM(21, true, false, POINT),
	POINTZ(11, true, true, POINT),

	MULTIPOINT(8, false, false),
	MULTIPOINTM(28, true, false, MULTIPOINT),
	MULTIPOINTZ(18, true, true, MULTIPOINT),

	POLYLINE(3, false, false),
	POLYLINEM(23, true, false, POLYLINE),
	POLYLINEZ(13, true, true, POLYLINE),

	POLYGON(5, false, false),
	POLYGONM(25, true, false, POLYGON),
	POLYGONZ(15, true, true, POLYGON),

	MULTIPATCH(31, true, true);
	
	private final int code;
	private ShapeType parent;
	public final boolean containsMeasure, containsZ;

	ShapeType(int code, boolean containsM, boolean containsZ, ShapeType parent) {
		this.code = code;
		this.containsMeasure = containsM;
		this.containsZ = containsZ;
		this.parent = parent;
	}

	ShapeType(int code, boolean containsM, boolean containsZ) {
		this(code, containsM, containsZ, null);
		this.parent = this;
	}
	
	public static ShapeType getType(int code) {
		for(ShapeType type : values()) {
			if(type.code == code) return type;
		}
		return null;
	}

	public ShapeType getParent() {
		return parent;
	}
}
