package com.mndk.kvm2m.core.vectormap.elem;

import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFPoint;

import com.mndk.kvm2m.core.projection.Projections;
import com.mndk.kvm2m.core.projection.grs80.Grs80Projection;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vectormap.DxfMapParser;
import com.mndk.kvm2m.core.vectormap.VectorMapObjectType;
import com.mndk.kvm2m.core.vectormap.elem.point.VectorMapElevationPoint;
import com.mndk.kvm2m.core.vectormap.elem.point.VectorMapPoint;
import com.mndk.kvm2m.core.vectormap.elem.poly.VectorMapContour;
import com.mndk.kvm2m.core.vectormap.elem.poly.VectorMapPolyline;

import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;

public abstract class VectorMapElement {

	
    private final VectorMapObjectType type;
    
    
    public VectorMapElement(VectorMapObjectType type) {
    	this.type = type;
    }
    
    
    protected static Vector2DH projectGrs80CoordToBteCoord(Grs80Projection projection, double x, double y) {
        double[] geoCoordinate = projection.toGeo(x, y), bteCoordinate;
        try {
            bteCoordinate = Projections.BTE.fromGeo(geoCoordinate[0], geoCoordinate[1]);
        } catch(OutOfProjectionBoundsException exception) {
            throw new RuntimeException(exception); // wcpgw lmao
        }
        return new Vector2DH(bteCoordinate[0] * Projections.BTE.metersPerUnit(), -bteCoordinate[1] * Projections.BTE.metersPerUnit());
    }

    
    
	public static void fromDXFLWPolyline(DXFLayer layer, DXFLWPolyline polyline, Grs80Projection projection, DxfMapParser.Result result) {
		
	    String layerName = layer.getName();
	    VectorMapObjectType type = VectorMapObjectType.getTypeFromLayerName(layerName);
	    
	    if(type == VectorMapObjectType.등고선) {
	    	
	    	VectorMapContour contour = new VectorMapContour(polyline, projection);
	    	for(Vector2DH v : contour.getVertexList()) {
	    		result.getElevationPoints().add(v.withHeight(contour.getElevation()));
	    	}
	    	
	    }
	    else if(type == VectorMapObjectType.도곽선) {
	    	result.setBoundary(new VectorMapPolyline(polyline, projection, VectorMapObjectType.도곽선));
	    }
	    else {
	    	result.getPolylines().add(new VectorMapPolyline(polyline, projection, type));
	    }
	}

	
	public static void fromDXFPoint(DXFLayer layer, DXFPoint point, Grs80Projection projection, DxfMapParser.Result result) {
		String layerName = layer.getName();
	    VectorMapObjectType type = VectorMapObjectType.getTypeFromLayerName(layerName);
		if(type == VectorMapObjectType.표고점) {
			result.getElevationPoints().add(new VectorMapElevationPoint(point, projection).toVector());
		}
		else {
		    result.getPoints().add(new VectorMapPoint(point, projection, type));
		}
	}
	
    
    public VectorMapObjectType getType() {
    	return type;
    }
}
