package com.mndk.kvm2m.core.vectorparser;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FilenameUtils;

import com.mndk.kvm2m.core.projection.Grs80Projection;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vectormap.VMapObjectType;
import com.mndk.kvm2m.core.vectormap.VMapParserResult;
import com.mndk.kvm2m.core.vectormap.VMapUtils;
import com.mndk.kvm2m.core.vectormap.elem.point.VMapElevationPoint;
import com.mndk.kvm2m.core.vectormap.elem.point.VMapPoint;
import com.mndk.kvm2m.core.vectormap.elem.poly.VMapBuilding;
import com.mndk.kvm2m.core.vectormap.elem.poly.VMapContour;
import com.mndk.kvm2m.core.vectormap.elem.poly.VMapPolyline;
import com.mndk.ngiparser.NgiParser;
import com.mndk.ngiparser.ngi.NgiLayer;
import com.mndk.ngiparser.ngi.NgiParserResult;
import com.mndk.ngiparser.ngi.element.NgiElement;
import com.mndk.ngiparser.ngi.element.NgiLineElement;
import com.mndk.ngiparser.ngi.element.NgiPointElement;
import com.mndk.ngiparser.ngi.element.NgiPolygonElement;

public class NgiMapParser {

	public static VMapParserResult parse(File mapFile) throws IOException {

		VMapParserResult result = new VMapParserResult();

		String fileName = mapFile.getName();
		if(!FilenameUtils.isExtension(fileName, "ngi")) return null;
		Grs80Projection projection = VMapUtils.getProjectionFromMapId(fileName.substring(0, fileName.length() - 4));
		NgiParserResult parseResult = NgiParser.parse(mapFile.getAbsolutePath(), "MS949", true);
		
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
	
	
	
	public static void fromNgiPolygonElement(NgiLayer layer, NgiPolygonElement polygon, Grs80Projection projection, VMapParserResult result) {
		
		String layerName = layer.name;
		VMapObjectType type = VMapObjectType.getTypeFromLayerName(layerName);

		if(type == VMapObjectType.도곽선) {
			result.setBoundary(new VMapPolyline(polygon, projection, VMapObjectType.도곽선));
		}
		else if(type == VMapObjectType.건물) {
			result.addElement(new VMapBuilding(polygon, projection, type));
		}
		else {
			result.addElement(new VMapPolyline(polygon, projection, type));
		}
	}
	
	
	
	public static void fromNgiLineElement(NgiLayer layer, NgiLineElement line, Grs80Projection projection, VMapParserResult result) {
		
		String layerName = layer.name;
		VMapObjectType type = VMapObjectType.getTypeFromLayerName(layerName);
		
		if(type == VMapObjectType.등고선) {
			VMapContour contour = new VMapContour(line, projection);
			for(Vector2DH[] va : contour.getVertexList()) for(Vector2DH v : va) {
				result.getElevationPoints().add(v.withHeight(contour.getElevation()));
			}
		}
		else if(type == VMapObjectType.도곽선) {
			result.setBoundary(new VMapPolyline(line, projection, VMapObjectType.도곽선));
		}
		else {
			result.addElement(new VMapPolyline(line, projection, type));
		}
	}
	
	
	
	public static void fromNgiPointElement(NgiLayer layer, NgiPointElement point, Grs80Projection projection, VMapParserResult result) {
		
		String layerName = layer.name;
		VMapObjectType type = VMapObjectType.getTypeFromLayerName(layerName);
		
		if(type == VMapObjectType.표고점) {
			result.getElevationPoints().add(new VMapElevationPoint(point, projection).toVector());
		}
		else {
			result.addElement(new VMapPoint(point, projection, type));
		}
	}
}
