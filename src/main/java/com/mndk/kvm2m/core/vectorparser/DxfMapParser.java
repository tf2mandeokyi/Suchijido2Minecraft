package com.mndk.kvm2m.core.vectorparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFPoint;
import org.kabeja.parser.DXFParser;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;

import com.mndk.kvm2m.core.projection.grs80.Grs80Projection;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vectormap.VectorMapObjectType;
import com.mndk.kvm2m.core.vectormap.VectorMapParserResult;
import com.mndk.kvm2m.core.vectormap.VectorMapUtils;
import com.mndk.kvm2m.core.vectormap.elem.point.VectorMapElevationPoint;
import com.mndk.kvm2m.core.vectormap.elem.point.VectorMapPoint;
import com.mndk.kvm2m.core.vectormap.elem.poly.VectorMapContour;
import com.mndk.kvm2m.core.vectormap.elem.poly.VectorMapPolyline;

public class DxfMapParser {

    @SuppressWarnings("unchecked")
	public static VectorMapParserResult parse(File mapFile) throws ParseException, FileNotFoundException {

    	VectorMapParserResult result = new VectorMapParserResult();

    	String fileName = mapFile.getName();
    	if(!FilenameUtils.isExtension(fileName, "dxf")) return null;
        Grs80Projection projection = VectorMapUtils.getProjectionFromMapId(fileName.substring(0, fileName.length() - 4));
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
                	fromDXFLWPolyline(layer, polyline, projection, result);
                }
            }
            if(layer.hasDXFEntities("POINT")) {
            	List<DXFPoint> points = layer.getDXFEntities("POINT");
            	for(DXFPoint point : points) {
            		fromDXFPoint(layer, point, projection, result);
            	}
            }
        }

        return result;

    }

    
    
	public static void fromDXFLWPolyline(DXFLayer layer, DXFLWPolyline polyline, Grs80Projection projection, VectorMapParserResult result) {
		
	    String layerName = layer.getName();
	    VectorMapObjectType type = VectorMapObjectType.getTypeFromLayerName(layerName);
	    
	    if(type == VectorMapObjectType.등고선) {
	    	
	    	VectorMapContour contour = new VectorMapContour(polyline, projection);
	        for(Vector2DH[] va : contour.getVertexList()) for(Vector2DH v : va) {
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

	
	
	public static void fromDXFPoint(DXFLayer layer, DXFPoint point, Grs80Projection projection, VectorMapParserResult result) {
		
		String layerName = layer.getName();
	    VectorMapObjectType type = VectorMapObjectType.getTypeFromLayerName(layerName);
	    
		if(type == VectorMapObjectType.표고점) {
			result.getElevationPoints().add(new VectorMapElevationPoint(point, projection).toVector());
		}
		else {
		    result.getPoints().add(new VectorMapPoint(point, projection, type));
		}
	}
    
}
