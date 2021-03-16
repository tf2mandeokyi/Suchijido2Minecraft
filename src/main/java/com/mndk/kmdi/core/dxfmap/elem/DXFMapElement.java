package com.mndk.kmdi.core.dxfmap.elem;

import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFPoint;

import com.mndk.kmdi.core.dxfmap.DXFMapObjectType;
import com.mndk.kmdi.core.dxfmap.DXFMapParser;
import com.mndk.kmdi.core.dxfmap.elem.point.DXFMapElevationPoint;
import com.mndk.kmdi.core.dxfmap.elem.point.DXFMapPointElement;
import com.mndk.kmdi.core.dxfmap.elem.polyline.DXFMapContour;
import com.mndk.kmdi.core.dxfmap.elem.polyline.DXFMapPolyline;
import com.mndk.kmdi.core.projection.Projections;
import com.mndk.kmdi.core.projection.grs80.Grs80Projection;
import com.sk89q.worldedit.Vector2D;

import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;

public abstract class DXFMapElement<T extends DXFEntity> {

	
    private final DXFMapObjectType type;
    
    
    public DXFMapElement(DXFMapObjectType type) {
    	this.type = type;
    }
    
    
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
	    DXFMapObjectType type = DXFMapObjectType.getTypeFromLayerName(layerName);
	    if(type == DXFMapObjectType.등고선) {
	    	DXFMapContour contour = new DXFMapContour(polyline, projection);
	    	for(Vector2D v : contour.getVertexList()) {
	    		result.getElevationPoints().add(v.toVector(contour.getElevation()));
	    	}
	    	// result.contourList.add(new DXFMapContour(polyline, projection));
	    }
	    else if(type == DXFMapObjectType.도곽선) {
	    	result.setBoundary(new DXFMapPolyline(polyline, projection, DXFMapObjectType.도곽선));
	    }
	    else {
	    	result.getPolylines().add(new DXFMapPolyline(polyline, projection, type));
	    }
	}

	
	public static void fromDXFPoint(DXFLayer layer, DXFPoint point, Grs80Projection projection, DXFMapParser.Result result) {
		String layerName = layer.getName();
	    DXFMapObjectType type = DXFMapObjectType.getTypeFromLayerName(layerName);
		if(type == DXFMapObjectType.표고점) {
			result.getElevationPoints().add(new DXFMapElevationPoint(point, projection).toVector());
		}
		else {
		    result.getPoints().add(new DXFMapPointElement(point, projection, type));
		}
	}
	
    
    public DXFMapObjectType getType() {
    	return type;
    }
}
