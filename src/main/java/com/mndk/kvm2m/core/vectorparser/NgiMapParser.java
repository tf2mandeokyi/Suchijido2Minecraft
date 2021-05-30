package com.mndk.kvm2m.core.vectorparser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vmap.VMapElementType;
import com.mndk.kvm2m.core.vmap.VMapParserResult;
import com.mndk.kvm2m.core.vmap.elem.VMapElement;
import com.mndk.kvm2m.core.vmap.elem.VMapElementLayer;
import com.mndk.kvm2m.core.vmap.elem.line.VMapContour;
import com.mndk.kvm2m.core.vmap.elem.line.VMapPolyline;
import com.mndk.kvm2m.core.vmap.elem.line.VMapWall;
import com.mndk.kvm2m.core.vmap.elem.point.VMapElevationPoint;
import com.mndk.kvm2m.core.vmap.elem.point.VMapPoint;
import com.mndk.kvm2m.core.vmap.elem.poly.VMapBuilding;
import com.mndk.kvm2m.core.vmap.elem.poly.VMapPolygon;
import com.mndk.ngiparser.NgiParser;
import com.mndk.ngiparser.nda.NdaDataColumn;
import com.mndk.ngiparser.ngi.NgiLayer;
import com.mndk.ngiparser.ngi.NgiParserResult;
import com.mndk.ngiparser.ngi.element.NgiLine;
import com.mndk.ngiparser.ngi.element.NgiMultiPolygon;
import com.mndk.ngiparser.ngi.element.NgiPoint;
import com.mndk.ngiparser.ngi.element.NgiPolygon;
import com.mndk.ngiparser.ngi.element.NgiRecord;
import com.mndk.ngiparser.ngi.vertex.NgiVector;

import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;

public class NgiMapParser extends VMapParser {


	@Override
	protected VMapParserResult getResult() throws IOException {

		VMapParserResult result = new VMapParserResult();
		NgiParserResult parseResult = NgiParser.parse(mapFile.getAbsolutePath(), "MS949", true);
		
		Collection<NgiLayer> layers = parseResult.getLayers().values();
		for(NgiLayer layer : layers) {
			if(layer.header.dimensions != 2) continue;
			VMapElementLayer elementLayer = fromNgiLayer(layer);
			result.addElement(elementLayer);
		}
		
		return result;
		
	}
	
	
	
	private VMapElementLayer fromNgiLayer(NgiLayer ngiLayer) {
		VMapElementType type = VMapElementType.fromLayerName(ngiLayer.name);
		
		NdaDataColumn[] columns = ngiLayer.header.columns;
		String[] layerColumns = new String[columns.length];
		for(int i = 0; i < columns.length; ++i) {
			layerColumns[i] = columns[i].name;
		}
		VMapElementLayer elementLayer = new VMapElementLayer(type, layerColumns);
		
		Collection<NgiRecord<?>> ngiElements = ngiLayer.data.values();
		for(NgiRecord<?> ngiElement : ngiElements) {
			VMapElement element = fromElement(elementLayer, ngiElement);
			if(element == null) continue;
			elementLayer.add(element);
		}
		
		return elementLayer;
	}
	
	
	
	private VMapElement fromElement(VMapElementLayer layer, NgiRecord<?> ngiElement) {
		if(ngiElement instanceof NgiMultiPolygon) {
			return fromMultiPolygon(layer, (NgiMultiPolygon) ngiElement);
		}
		else if(ngiElement instanceof NgiPolygon) {
			return fromPolygon(layer, (NgiPolygon) ngiElement);
		}
		else if(ngiElement instanceof NgiLine) {
			return fromLine(layer, (NgiLine) ngiElement);
		}
		else if(ngiElement instanceof NgiPoint) {
			return fromPoint(layer, (NgiPoint) ngiElement);
		}
		return null;
	}



	private VMapElement fromMultiPolygon(VMapElementLayer layer, NgiMultiPolygon polygon) {
		List<Vector2DH[]> vertexList = new ArrayList<Vector2DH[]>();
		Vector2DH[] tempArray;
		
		for(int k = 0; k < polygon.vertexData.length; ++k) {
			for (int j = 0; j < polygon.vertexData[k].length; ++j) {
				int size = polygon.vertexData[k][j].getSize();
				vertexList.add(tempArray = new Vector2DH[size]);

				for (int i = 0; i < size; ++i) {
					NgiVector vertex = polygon.vertexData[k][j].getVertex(i);
					tempArray[i] = this.targetProjToWorldProjCoord(vertex.getAxis(0), vertex.getAxis(1));
				}
			}
		}
		
		Vector2DH[][] vertexArray = vertexList.toArray(new Vector2DH[0][]);

		if(layer.getType() == VMapElementType.건물) {
			if(options.containsKey("gen-building-shells")) {
				return new VMapBuilding(layer, vertexArray, polygon.rowData);
			} else {
				return new VMapPolyline(layer, vertexArray, polygon.rowData, true);
			}
		}
		else { return new VMapPolygon(layer, vertexArray, polygon.rowData, true); }
	}
	
	
	
	private VMapElement fromPolygon(VMapElementLayer layer, NgiPolygon polygon) {
		Vector2DH[][] vertexList = new Vector2DH[polygon.vertexData.length][];
		
		for(int j = 0; j < polygon.vertexData.length; ++j) {
			int size = polygon.vertexData[j].getSize();
			vertexList[j] = new Vector2DH[size];
			
			for(int i = 0; i < size; ++i) {
				NgiVector vertex = polygon.vertexData[0].getVertex(i);
				vertexList[j][i] = this.targetProjToWorldProjCoord(vertex.getAxis(0), vertex.getAxis(1));
			}
		}
		
		if(layer.getType() == VMapElementType.건물) {
			if(options.containsKey("gen-building-shells")) { 
				return new VMapBuilding(layer, vertexList, polygon.rowData);
			} else {
				return new VMapPolyline(layer, vertexList, polygon.rowData, true);
			}
		}
		else { return new VMapPolygon(layer, vertexList, polygon.rowData, true); }
	}
	

	
	private VMapPolyline fromLine(VMapElementLayer layer, NgiLine line) {
		int size = line.lineData.getSize();
		Vector2DH[] vertexList = new Vector2DH[size];
		
		for(int i = 0; i < size; ++i) {
			NgiVector vertex = line.lineData.getVertex(i);
			vertexList[i] = this.targetProjToWorldProjCoord(vertex.getAxis(0), vertex.getAxis(1));
		}
		
		if(layer.getType() == VMapElementType.등고선) { return new VMapContour(layer, vertexList, line.rowData); }
		else if(layer.getType() == VMapElementType.옹벽) { return new VMapWall(layer, new Vector2DH[][] {vertexList}, line.rowData, false); }
		else { return new VMapPolyline(layer, new Vector2DH[][] {vertexList}, line.rowData, false); }
	}
	
	
	
	private VMapPoint fromPoint(VMapElementLayer layer, NgiPoint point) {
		Vector2DH vpoint = this.targetProjToWorldProjCoord(point.position.getAxis(0), point.position.getAxis(1));
		
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
		
		Map<String, String> emptyOption = new HashMap<>();
		
		NgiMapParser parser = new NgiMapParser();
		
		VMapParserResult result = new VMapParserResult();
		result.append(parser.parse(new File("test/376081986.ngi"), BTE, emptyOption));
		result.append(parser.parse(new File("test/377052193.ngi"), BTE, emptyOption));
		result.append(parser.parse(new File("test/376082465.ngi"), BTE, emptyOption));
		for(VMapElementLayer layer : result.getElementLayers()) {
			System.out.println(layer.getType() + ": " + layer.size());
			/*for(VMapElement element : layer) {
				System.out.println("  " + element.getParent().getType() + " (" + element.getDataByColumn("UFID") + ")");
			}*/
		}
	}
	
}
