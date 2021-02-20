package com.mndk.kmdi.core.dxfmap.elem;

import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFPoint;

import com.mndk.kmdi.core.dxfmap.DXFMapParser;
import com.mndk.kmdi.core.dxfmap.elem.point.DXFMapElevationPoint;
import com.mndk.kmdi.core.dxfmap.elem.polyline.DXFMapContour;
import com.mndk.kmdi.core.dxfmap.elem.polyline.DXFMapPolyline;
import com.mndk.kmdi.core.projection.Projections;
import com.mndk.kmdi.core.projection.grs80.Grs80Projection;
import com.sk89q.worldedit.Vector2D;

import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;

public class DXFMapElement<T extends DXFEntity> {

    protected static Vector2D projectGrs80CoordToBteCoord(Grs80Projection projection, double x, double y) {
        double[] geoCoordinate = projection.toGeo(x, y), bteCoordinate;
        try {
            bteCoordinate = Projections.BTE.fromGeo(geoCoordinate[0], geoCoordinate[1]);
        } catch(OutOfProjectionBoundsException exception) {
            throw new RuntimeException(exception); // wcpgw lmao
        }
        return new Vector2D(bteCoordinate[0] * Projections.BTE.metersPerUnit(), -bteCoordinate[1] * Projections.BTE.metersPerUnit());
    }

	public static void fromDXFLWPolyline(DXFLayer layer, DXFLWPolyline polyline, Grs80Projection projection, DXFMapParser.Result result) {
	    String layerName = layer.getName();
	    if(layerName.startsWith("F001")) {
	    	result.contourList.add(new DXFMapContour(polyline, projection));
	    }
	    else if(layerName.startsWith("B001")) {
	    	result.polylineList.add(new /*DXFMapBuilding*/DXFMapPolyline(polyline, projection));
	    }
	    else if(layerName.startsWith("H001")) {
	    	result.boundary = new DXFMapPolyline(polyline, projection);
	    }
	}

	public static void fromDXFPoint(DXFLayer layer, DXFPoint point, Grs80Projection projection, DXFMapParser.Result result) {
		String layerName = layer.getName();
		if(layerName.startsWith("F002")) {
			result.pointList.add(new DXFMapElevationPoint(point, projection));
		}
	}
}
