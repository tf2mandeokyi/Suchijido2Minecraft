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
import com.mndk.kmdi.core.projection.grs80.Grs80Projection;

public class DXFMapParser {

    @SuppressWarnings("unchecked")
	public static Result parse(File mapFile) throws ParseException, FileNotFoundException {

    	Result result = new Result();

    	String fileName = mapFile.getName();
    	if(!fileName.endsWith(".dxf")) return null;
        Grs80Projection projection = DXFMapProperties.getProjectionFromMapId(fileName.substring(0, fileName.length() - 4));
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
    
    /*
     *  === LAYER IDS ===
     * (A) - Area // Seems that it's a line but closed version
     * (L) - Line
     * (P) - Point
     * 
     * TODO implement these all... I guess?
     * A - Traffic
     * A001 - (A/L) Road boundary
     * A002 - (L) Road center line
     * A003 - (L) Pedestrian road
     * A004 - (A) Crosswalk
     * A005 - (A) Safe zone
     * A006 - (A) Pedestrian Overpass
     * A007 - (A) Bridge
     * A008 - (A) Crossroad
     * A009 - (A) Multi-level Crossing
     * A010 - (A) Interchange // Why is this a thing lmao
     * A011 - (A) Tunnel
     * A012 - (L) Tunnel Entrance
     * A013 - (P) Train Station
     * A014 - (P) Bus station?
     * A015 - (L) Railways
     * A016 - (A) Railway boundaries?
     * A017 - (L) Railway center line
     * A018 - (P) Railway tram stand?
     * A019 - (A) Platform
     * A020 - (A) Platform Roof
     * A021 - (P) Port?
     * A022 - (L) Ferry routes?
     * 
     * B - Buildings
     * B001 - (A) Building
     * B002 - (L) Wall
     * 
     * C - Facility
     * C001 - (A) Dam
     * C002 - (A) Wharf
     * C003 - (A) Dock
     * C004 - (A) Dock?
     * C005 - (L) Embankment
     * C006 - (L) Sluice gate
     * C007 - (L) Culvert?
     * C008 - (L) 
     * C009 - (P) 
     * C010 - (P) 
     * C011 - (P) 
     * C012 - (P) 
     * C013 - (P) 
     * C014 - (P) 
     * C015 - (P) 
     * C016 - (P) 
     * C017 - (A) 
     * C018 - (A) 
     * C019 - (P) 
     * C020 - (P) 
     * C021 - (P) 
     * C022 - (P) 
     * C023 - (P) 
     * C024 - (P) 
     * C025 - (P) 
     * C026 - (P) 
     * C027 - (A) 
     * C028 - (P) 
     * C029 - (A) 
     * C030 - (A) 
     * C031 - (P) 
     * C032 - (L) 
     * C033 - (P) 
     * C034 - (P) 
     * C035 - (P) 
     * C036 - (P) 
     * C037 - (P) 
     * C038 - (P) 
     * C039 - (A) 
     * C040 - (P) 
     * C041 - (P) 
     * C042 - (A) 
     * C043 - (A) 
     * C044 - (A) 
     * C045 - (A) 
     * C046 - (L) 
     * C047 - (A) 
     * C048 - (P) 
     * C049 - (P) 
     * C050 - (P) 
     * C051 - (P) 
     * C052 - (L) 
     * C053 - (L) 
     * C054 - (P) 
     * C055 - (P) 
     * 
     * D - Vegetation
     * D001 ~ D004
     * 
     * E - Water system?
     * E001 ~ E008
     * 
     * F - Terrain
     * F001 ~ F005
     * 
     * G - Boundaries
     * G001 ~ G003, G010 ~ G011
     * 
     * H - Misc.
     * H001 - DXF Boundary
     * H002 - Center
     * ... H005
     * */
    
}
