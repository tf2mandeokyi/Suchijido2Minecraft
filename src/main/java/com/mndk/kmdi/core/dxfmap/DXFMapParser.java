package com.mndk.kmdi.core.dxfmap;

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

import com.mndk.kmdi.core.dxfmap.elem.DXFMapElement;
import com.mndk.kmdi.core.dxfmap.elem.point.DXFMapPointElement;
import com.mndk.kmdi.core.dxfmap.elem.polyline.DXFMapContour;
import com.mndk.kmdi.core.dxfmap.elem.polyline.DXFMapPolyline;
import com.mndk.kmdi.core.projection.Projections;
import com.mndk.kmdi.core.projection.grs80.Grs80Projection;

public class DXFMapParser {

    @SuppressWarnings("unchecked")
	public static Result parse(File mapFile) throws ParseException, FileNotFoundException {

    	Result result = new Result();

    	String fileName = mapFile.getName();
    	if(!fileName.endsWith(".dxf")) return null;
        Grs80Projection projection = DXFMapParser.getProjectionFromMapId(fileName.substring(0, fileName.length() - 4));
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
                	DXFMapElement.fromDXFLWPolyline(layer, polyline, projection, result);
                }
            }
            if(layer.hasDXFEntities("POINT")) {
            	List<DXFPoint> points = layer.getDXFEntities("POINT");
            	for(DXFPoint point : points) {
            		DXFMapElement.fromDXFPoint(layer, point, projection, result);
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
	            if(last >= '0' && last <= '9') // If the last character is a number:
	                return 5000;
	            else // Or else if it's an alphabet
	                return 2500;
	        case 9: return 1000;
	        case 10: return 500;
	    }
	    return -1;
	}
	
	

    public static class Result {
    	public DXFMapPolyline boundary;
    	public List<DXFMapPolyline> polylineList;
    	public List<DXFMapPointElement> pointList;
    	public List<DXFMapContour> contourList;
    	private Result() {
    		this.polylineList = new ArrayList<>();
    		this.pointList = new ArrayList<>();
    		this.contourList = new ArrayList<>();
    	}
    	public DXFMapPolyline getBoundary() {
    		return boundary;
    	}
    	public List<DXFMapPolyline> getPolylines() {
    		return polylineList;
    	}
    	public List<DXFMapPointElement> getPoints() {
    		return pointList;
    	}
    	public List<DXFMapContour> getContourList() {
    		return contourList;
    	}
    }
    
}
