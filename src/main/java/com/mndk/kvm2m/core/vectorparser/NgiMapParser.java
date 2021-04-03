package com.mndk.kvm2m.core.vectorparser;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.mndk.kvm2m.core.projection.Grs80Projection;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vectormap.VMapElementType;
import com.mndk.kvm2m.core.vectormap.VMapParserResult;
import com.mndk.kvm2m.core.vectormap.elem.VMapElement;
import com.mndk.kvm2m.core.vectormap.elem.VMapElementLayer;
import com.mndk.kvm2m.core.vectormap.elem.point.VMapElevationPoint;
import com.mndk.kvm2m.core.vectormap.elem.point.VMapPoint;
import com.mndk.kvm2m.core.vectormap.elem.poly.VMapBuilding;
import com.mndk.kvm2m.core.vectormap.elem.poly.VMapContour;
import com.mndk.kvm2m.core.vectormap.elem.poly.VMapPolyline;
import com.mndk.ngiparser.NgiParser;
import com.mndk.ngiparser.nda.NdaDataColumn;
import com.mndk.ngiparser.ngi.NgiLayer;
import com.mndk.ngiparser.ngi.NgiParserResult;
import com.mndk.ngiparser.ngi.element.NgiElement;
import com.mndk.ngiparser.ngi.element.NgiLineElement;
import com.mndk.ngiparser.ngi.element.NgiPointElement;
import com.mndk.ngiparser.ngi.element.NgiPolygonElement;
import com.mndk.ngiparser.ngi.vertex.NgiVertex;

public class NgiMapParser extends VMapParser {

	public VMapParserResult parse(File mapFile) throws IOException {

		VMapParserResult result = new VMapParserResult();

		Grs80Projection projection = getProjFromFileName(mapFile);
		NgiParserResult parseResult = NgiParser.parse(mapFile.getAbsolutePath(), "MS949", true);
		
		Collection<NgiLayer> layers = parseResult.getLayers().values();
		for(NgiLayer layer : layers) {
			if(layer.header.dimensions != 2) continue;
			VMapElementLayer elementLayer = fromNgiLayer(layer, projection, result.getElevationPoints());
			result.addElement(elementLayer);
		}
		
		return result;
		
	}
	
	
	
	private static VMapElementLayer fromNgiLayer(NgiLayer ngiLayer, Grs80Projection projection, List<Vector2DH> elevPoints) {
		VMapElementType type = VMapElementType.getTypeFromLayerName(ngiLayer.name);
		
		NdaDataColumn[] columns = ngiLayer.header.columns;
		String[] layerColumns = new String[columns.length];
		for(int i = 0; i < columns.length; ++i) {
			layerColumns[i] = columns[i].name;
		}
		VMapElementLayer elementLayer = new VMapElementLayer(type, layerColumns);
		
		Collection<NgiElement<?>> ngiElements = ngiLayer.data.values();
		for(NgiElement<?> ngiElement : ngiElements) {
			VMapElement element = fromNgiElement(elementLayer, ngiElement, projection);
			if(element == null) continue;
			elementLayer.add(element);
			extractElevationPoints(element, elevPoints);
		}
		
		return elementLayer;
	}
	
	
	

	
	
	private static VMapElement fromNgiElement(VMapElementLayer layer, NgiElement<?> ngiElement, Grs80Projection projection) {
		if(ngiElement instanceof NgiPolygonElement) {
			return fromNgiPolygon(layer, (NgiPolygonElement) ngiElement, projection);
		}
		else if(ngiElement instanceof NgiLineElement) {
			return fromNgiLine(layer, (NgiLineElement) ngiElement, projection);
		}
		else if(ngiElement instanceof NgiPointElement) {
			return fromNgiPoint(layer, (NgiPointElement) ngiElement, projection);
		}
		return null;
	}
	
	
	private static VMapPolyline fromNgiPolygon(
			VMapElementLayer layer, 
			NgiPolygonElement polygon, 
			Grs80Projection projection
	) {
		Vector2DH[][] vertexList = new Vector2DH[polygon.vertexData.length][];
		
		for(int j = 0; j < polygon.vertexData.length; ++j) {
			int size = polygon.vertexData[j].getSize();
			vertexList[j] = new Vector2DH[size];
			
			for(int i = 0; i < size; ++i) {
				NgiVertex vertex = polygon.vertexData[0].getVertex(i);
				vertexList[j][i] = projectGrs80CoordToBteCoord(projection, vertex.getAxis(0), vertex.getAxis(1));
			}
		}
		
		if(layer.getType() == VMapElementType.건물) { return new VMapBuilding(layer, vertexList, polygon.rowData); }
		else { return new VMapPolyline(layer, vertexList, polygon.rowData, true); }
	}
	
	
	
	private static VMapPolyline fromNgiLine(
			VMapElementLayer layer, 
			NgiLineElement line, 
			Grs80Projection projection
	) {
		int size = line.lineData.getSize();
		Vector2DH[] vertexList = new Vector2DH[size];
		
		for(int i = 0; i < size; ++i) {
			NgiVertex vertex = line.lineData.getVertex(i);
			vertexList[i] = projectGrs80CoordToBteCoord(projection, vertex.getAxis(0), vertex.getAxis(1));
		}
		
		if(layer.getType() == VMapElementType.등고선) { return new VMapContour(layer, vertexList, line.rowData); }
		else { return new VMapPolyline(layer, vertexList, line.rowData, false); }
	}
	
	
	
	private static VMapPoint fromNgiPoint(
			VMapElementLayer layer, 
			NgiPointElement point, 
			Grs80Projection projection
	) {
		Vector2DH vpoint = projectGrs80CoordToBteCoord(projection, point.position.getAxis(0), point.position.getAxis(1));
		
		if(layer.getType() == VMapElementType.표고점) { return new VMapElevationPoint(layer, vpoint, point.rowData); }
		else { return new VMapPoint(layer, vpoint, point.rowData); }
	}
	
	
	
	public static void main(String[] args) throws IOException {
		VMapParserResult result = new NgiMapParser().parse(new File("test/376081986.ngi"));
		for(VMapElementLayer layer : result.getElementLayers()) {
			System.out.println(layer.getType() + ": " + layer.size());
			for(VMapElement element : layer) {
				System.out.println("  " + element.getParent().getType() + " (" + element.getDataByColumn("UFID") + ")");
			}
		}
	}
	
}
