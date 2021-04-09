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

import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;

public class NgiMapParser extends VMapParser {


	@Override
	public VMapParserResult parse(File mapFile, GeographicProjection worldProjection) throws IOException {

		VMapParserResult result = new VMapParserResult();

		Grs80Projection projection = getProjFromFileName(mapFile);
		NgiParserResult parseResult = NgiParser.parse(mapFile.getAbsolutePath(), "MS949", true);
		
		Collection<NgiLayer> layers = parseResult.getLayers().values();
		for(NgiLayer layer : layers) {
			if(layer.header.dimensions != 2) continue;
			VMapElementLayer elementLayer = fromNgiLayer(layer, result.getElevationPoints(), projection, worldProjection);
			result.addElement(elementLayer);
		}
		
		return result;
		
	}
	
	
	
	private static VMapElementLayer fromNgiLayer(
			NgiLayer ngiLayer,
			List<Vector2DH> elevPoints,
			Grs80Projection projection,
			GeographicProjection worldProjection
	) {
		VMapElementType type = VMapElementType.getTypeFromLayerName(ngiLayer.name);
		
		NdaDataColumn[] columns = ngiLayer.header.columns;
		String[] layerColumns = new String[columns.length];
		for(int i = 0; i < columns.length; ++i) {
			layerColumns[i] = columns[i].name;
		}
		VMapElementLayer elementLayer = new VMapElementLayer(type, layerColumns);
		
		Collection<NgiElement<?>> ngiElements = ngiLayer.data.values();
		for(NgiElement<?> ngiElement : ngiElements) {
			VMapElement element = fromNgiElement(elementLayer, ngiElement, projection, worldProjection);
			if(element == null) continue;
			elementLayer.add(element);
			extractElevationPoints(element, elevPoints);
		}
		
		return elementLayer;
	}
	
	
	

	
	
	private static VMapElement fromNgiElement(
			VMapElementLayer layer,
			NgiElement<?> ngiElement,
			Grs80Projection projection,
			GeographicProjection worldProjection
	) {
		if(ngiElement instanceof NgiPolygonElement) {
			return fromNgiPolygon(layer, (NgiPolygonElement) ngiElement, projection, worldProjection);
		}
		else if(ngiElement instanceof NgiLineElement) {
			return fromNgiLine(layer, (NgiLineElement) ngiElement, projection, worldProjection);
		}
		else if(ngiElement instanceof NgiPointElement) {
			return fromNgiPoint(layer, (NgiPointElement) ngiElement, projection, worldProjection);
		}
		return null;
	}
	
	
	private static VMapPolyline fromNgiPolygon(
			VMapElementLayer layer, 
			NgiPolygonElement polygon, 
			Grs80Projection projection,
			GeographicProjection worldProjection
	) {
		Vector2DH[][] vertexList = new Vector2DH[polygon.vertexData.length][];
		
		for(int j = 0; j < polygon.vertexData.length; ++j) {
			int size = polygon.vertexData[j].getSize();
			vertexList[j] = new Vector2DH[size];
			
			for(int i = 0; i < size; ++i) {
				NgiVertex vertex = polygon.vertexData[0].getVertex(i);
				vertexList[j][i] = projectGrs80CoordToWorldCoord(projection, worldProjection, vertex.getAxis(0), vertex.getAxis(1));
			}
		}
		
		if(layer.getType() == VMapElementType.건물) { return new VMapBuilding(layer, vertexList, polygon.rowData); }
		else { return new VMapPolyline(layer, vertexList, polygon.rowData, true); }
	}
	
	
	
	private static VMapPolyline fromNgiLine(
			VMapElementLayer layer, 
			NgiLineElement line, 
			Grs80Projection projection,
			GeographicProjection worldProjection
	) {
		int size = line.lineData.getSize();
		Vector2DH[] vertexList = new Vector2DH[size];
		
		for(int i = 0; i < size; ++i) {
			NgiVertex vertex = line.lineData.getVertex(i);
			vertexList[i] = projectGrs80CoordToWorldCoord(projection, worldProjection, vertex.getAxis(0), vertex.getAxis(1));
		}
		
		if(layer.getType() == VMapElementType.등고선) { return new VMapContour(layer, vertexList, line.rowData); }
		else { return new VMapPolyline(layer, vertexList, line.rowData, false); }
	}
	
	
	
	private static VMapPoint fromNgiPoint(
			VMapElementLayer layer, 
			NgiPointElement point, 
			Grs80Projection projection,
			GeographicProjection worldProjection
	) {
		Vector2DH vpoint = projectGrs80CoordToWorldCoord(projection, worldProjection, point.position.getAxis(0), point.position.getAxis(1));
		
		if(layer.getType() == VMapElementType.표고점) { return new VMapElevationPoint(layer, vpoint, point.rowData); }
		else { return new VMapPoint(layer, vpoint, point.rowData); }
	}
	
	
	
	public static void main(String[] args) throws IOException {
		
		String BTE_GEN_JSON =
				"{" +
					"\"projection\":\"bteairocean\"," +
					"\"orentation\":\"upright\"," +
					"\"scaleX\":7318261.522857145," +
					"\"scaleY\":7318261.522857145" +
				"}";
		GeographicProjection BTE = EarthGeneratorSettings.parse(BTE_GEN_JSON).projection();
		
		VMapParserResult result = new VMapParserResult();
		result.append(new NgiMapParser().parse(new File("test/376081986.ngi"), BTE));
		result.append(new NgiMapParser().parse(new File("test/377052193.ngi"), BTE));
		for(VMapElementLayer layer : result.getElementLayers()) {
			System.out.println(layer.getType() + ": " + layer.size());
			/*for(VMapElement element : layer) {
				System.out.println("  " + element.getParent().getType() + " (" + element.getDataByColumn("UFID") + ")");
			}*/
		}
	}
	
}
