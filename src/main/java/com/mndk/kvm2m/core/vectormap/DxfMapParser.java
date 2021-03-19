package com.mndk.kvm2m.core.vectormap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFPoint;
import org.kabeja.parser.DXFParser;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;

import com.mndk.kvm2m.core.projection.Projections;
import com.mndk.kvm2m.core.projection.grs80.Grs80Projection;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vectormap.elem.VectorMapElement;
import com.mndk.kvm2m.core.vectormap.elem.point.VectorMapPoint;
import com.mndk.kvm2m.core.vectormap.elem.poly.VectorMapPolyline;

public class DxfMapParser {

    @SuppressWarnings("unchecked")
	public static Result parse(File mapFile) throws ParseException, FileNotFoundException {

    	Result result = new Result();

    	String fileName = mapFile.getName();
    	if(!fileName.endsWith(".dxf")) return null;
        Grs80Projection projection = DxfMapParser.getProjectionFromMapId(fileName.substring(0, fileName.length() - 4));
        Parser parser = ParserBuilder.createDefaultParser();
        parser.parse(
                new FileInputStream(mapFile),
                DXFParser.DEFAULT_ENCODING
        );
        DXFDocument doc = parser.getDocument();
        Iterator<DXFLayer> layers = doc.getDXFLayerIterator();

        while(layers.hasNext()) {
            DXFLayer layer = layers.next();
            if(layer.hasDXFEntities("LWPOLYLINE")) {
                List<DXFLWPolyline> polylines = layer.getDXFEntities("LWPOLYLINE");
                for(DXFLWPolyline polyline : polylines) {
                	VectorMapElement.fromDXFLWPolyline(layer, polyline, projection, result);
                }
            }
            if(layer.hasDXFEntities("POINT")) {
            	List<DXFPoint> points = layer.getDXFEntities("POINT");
            	for(DXFPoint point : points) {
            		VectorMapElement.fromDXFPoint(layer, point, projection, result);
            	}
            }
        }

        return result;

    }

    
    
	static Grs80Projection getProjectionFromMapId(String fileName) {
	    char number = fileName.charAt(2);
	    if(number == '5') {
	        return Projections.GRS80_WEST;
	    } else if(number == '6' || number == '7') {
	        return Projections.GRS80_MIDDLE;
	    } else if(number == '8' || number == '9') {
	        return Projections.GRS80_EAST;
	    }
	    return null;
	}

	
	
	public static int getScaleFromMapId(String id) {
	    switch(id.length()) {
	        case 3: return 250000;
	        case 5: return 50000;
	        case 6: return 25000;
	        case 7: return 10000;
	        case 8:
	            char last = id.charAt(7);
	            if(last >= '0' && last <= '9') // If the last character is v1 number:
	                return 5000;
	            else // Or else if it's an alphabet
	                return 2500;
	        case 9: return 1000;
	        case 10: return 500;
	    }
	    return -1;
	}
	
	

    public static class Result {
    	private VectorMapPolyline boundary;
    	private List<VectorMapPolyline> polylineList;
    	private List<VectorMapPoint> pointList;
    	private List<Vector2DH> elevationPointList;
    	private Result() {
    		this.polylineList = new ArrayList<>();
    		this.pointList = new ArrayList<>();
    		this.elevationPointList = new ArrayList<>();
    	}
    	public VectorMapPolyline getBoundary() {
    		return boundary;
    	}
    	public void setBoundary(VectorMapPolyline boundary) {
    		this.boundary = boundary;
    	}
    	public List<VectorMapPolyline> getPolylines() {
    		return polylineList;
    	}
    	public List<VectorMapPoint> getPoints() {
    		return pointList;
    	}
    	public List<Vector2DH> getElevationPoints() {
    		return elevationPointList;
    	}
    }
    
}
