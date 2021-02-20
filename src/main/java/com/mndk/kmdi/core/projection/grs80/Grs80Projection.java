package com.mndk.kmdi.core.projection.grs80;

import com.mndk.kmdi.core.projection.Proj4jProjection;

public class Grs80Projection extends Proj4jProjection {
    public Grs80Projection(String crsName, double lon_0) {
        super(crsName, new String[]{
                "+proj=tmerc", "+lat_0=38", "+lon_0=" + lon_0, "+k=1", "+x_0=200000", "+y_0=600000", "+ellps=GRS80", "+units=m", "+no_defs"
        });
    }

    public static class Grs80WestProjection extends Grs80Projection {
        public Grs80WestProjection() { super("EPSG:5185", 125); }
    }

    public static class Grs80MiddleProjection extends Grs80Projection {
        public Grs80MiddleProjection() { super("EPSG:5186", 127); }
    }

    public static class Grs80EastProjection extends Grs80Projection {
        public Grs80EastProjection() { super("EPSG:5187", 129); }
    }
}
