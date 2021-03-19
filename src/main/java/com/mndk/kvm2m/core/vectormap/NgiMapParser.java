package com.mndk.kvm2m.core.vectormap;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FilenameUtils;

import com.mndk.kvm2m.core.projection.grs80.Grs80Projection;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vectormap.elem.point.VectorMapElevationPoint;
import com.mndk.kvm2m.core.vectormap.elem.point.VectorMapPoint;
import com.mndk.kvm2m.core.vectormap.elem.poly.VectorMapContour;
import com.mndk.kvm2m.core.vectormap.elem.poly.VectorMapPolyline;
import com.mndk.ngiparser.NgiParser;
import com.mndk.ngiparser.ngi.NgiLayer;
import com.mndk.ngiparser.ngi.NgiParseResult;
import com.mndk.ngiparser.ngi.element.NgiElement;
import com.mndk.ngiparser.ngi.element.NgiLineElement;
import com.mndk.ngiparser.ngi.element.NgiPointElement;
import com.mndk.ngiparser.ngi.element.NgiPolygonElement;

public class NgiMapParser {

	public static VectorMapParserResult parse(File mapFile) throws IOException {

    	VectorMapParserResult result = new VectorMapParserResult();

    	String fileName = mapFile.getName();
    	if(!FilenameUtils.isExtension(fileName, "ngi")) return null;
        Grs80Projection projection = VectorMapUtils.getProjectionFromMapId(fileName.substring(0, fileName.length() - 4));
        NgiParseResult parseResult = NgiParser.parse(mapFile.getAbsolutePath(), "MS949", true);
        
        Collection<NgiLayer> layers = parseResult.getLayers().values();
        for(NgiLayer layer : layers) {
        	if(layer.header.dimensions != 2) continue;
        	Collection<NgiElement<?>> elements = layer.data.values();
        	for(NgiElement<?> element : elements) {
        		if(element instanceof NgiPolygonElement) {
        			fromNgiPolygonElement(layer, (NgiPolygonElement) element, projection, result);
        		}
        		else if(element instanceof NgiLineElement) {
        			fromNgiLineElement(layer, (NgiLineElement) element, projection, result);
        		}
        		else if(element instanceof NgiPointElement) {
        			fromNgiPointElement(layer, (NgiPointElement) element, projection, result);
        		}
        	}
        }
        
        return result;
    	
	}
	
	
	
	public static void fromNgiPolygonElement(NgiLayer layer, NgiPolygonElement polygon, Grs80Projection projection, VectorMapParserResult result) {
		
		String layerName = layer.name;
		VectorMapObjectType type = VectorMapObjectType.getTypeFromLayerName(layerName);

	    if(type == VectorMapObjectType.도곽선) {
	    	result.setBoundary(new VectorMapPolyline(polygon, projection, VectorMapObjectType.도곽선));
	    }
	    else {
	    	result.getPolylines().add(new VectorMapPolyline(polygon, projection, type));
	    }
	}
	
	
	
	public static void fromNgiLineElement(NgiLayer layer, NgiLineElement line, Grs80Projection projection, VectorMapParserResult result) {
		
		String layerName = layer.name;
		VectorMapObjectType type = VectorMapObjectType.getTypeFromLayerName(layerName);
		
		if(type == VectorMapObjectType.등고선) {
	    	VectorMapContour contour = new VectorMapContour(line, projection);
	    	for(Vector2DH v : contour.getVertexList()) {
	    		result.getElevationPoints().add(v.withHeight(contour.getElevation()));
	    	}
	    }
	    else {
	    	result.getPolylines().add(new VectorMapPolyline(line, projection, type));
	    }
	}
	
	
	
	public static void fromNgiPointElement(NgiLayer layer, NgiPointElement point, Grs80Projection projection, VectorMapParserResult result) {
		
		String layerName = layer.name;
		VectorMapObjectType type = VectorMapObjectType.getTypeFromLayerName(layerName);
		
		if(type == VectorMapObjectType.표고점) {
			result.getElevationPoints().add(new VectorMapElevationPoint(point, projection).toVector());
		}
		else {
		    result.getPoints().add(new VectorMapPoint(point, projection, type));
		}
	}
	
	
	
	public static void main(String[] args) throws IOException {
		NgiMapParser.parse(new File("376081986.ngi"));
	}
}