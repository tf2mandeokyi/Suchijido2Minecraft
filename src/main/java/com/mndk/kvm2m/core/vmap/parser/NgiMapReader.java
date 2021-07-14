package com.mndk.kvm2m.core.vmap.parser;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vmap.VMapElementType;
import com.mndk.kvm2m.core.vmap.VMapReaderResult;
import com.mndk.kvm2m.core.vmap.elem.VMapElement;
import com.mndk.kvm2m.core.vmap.elem.VMapLayer;
import com.mndk.kvm2m.core.vmap.elem.line.VMapContour;
import com.mndk.kvm2m.core.vmap.elem.line.VMapPolyline;
import com.mndk.kvm2m.core.vmap.elem.line.VMapWall;
import com.mndk.kvm2m.core.vmap.elem.point.VMapElevationPoint;
import com.mndk.kvm2m.core.vmap.elem.point.VMapPoint;
import com.mndk.kvm2m.core.vmap.elem.poly.VMapBuilding;
import com.mndk.kvm2m.core.vmap.elem.poly.VMapPolygon;
import com.mndk.kvm2m.mod.KVectorMap2MinecraftMod;
import com.mndk.ngiparser.NgiParser;
import com.mndk.ngiparser.nda.NdaDataColumn;
import com.mndk.ngiparser.ngi.NgiLayer;
import com.mndk.ngiparser.ngi.NgiParserResult;
import com.mndk.ngiparser.ngi.element.*;
import com.mndk.ngiparser.ngi.vertex.NgiVector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NgiMapReader extends VMapReader {


	@Override
	protected VMapReaderResult getResult() throws IOException {

		VMapReaderResult result = new VMapReaderResult();
		NgiParserResult parseResult = NgiParser.parse(mapFile.getAbsolutePath(), "MS949", true);
		
		Collection<NgiLayer> layers = parseResult.getLayers().values();
		for(NgiLayer layer : layers) {
			if(layer.header.dimensions != 2) continue;
			try {
				VMapLayer elementLayer = fromNgiLayer(layer);
				result.addElement(elementLayer);
			} catch(NullPointerException ignored) {} // TODO I don't have a good feeling about this
		}
		
		return result;
		
	}
	
	
	
	private VMapLayer fromNgiLayer(NgiLayer ngiLayer) {
		VMapElementType type = VMapElementType.fromLayerName(ngiLayer.name);
		
		NdaDataColumn[] columns = ngiLayer.header.columns;
		String[] layerColumns = new String[columns.length];
		for(int i = 0; i < columns.length; ++i) {
			layerColumns[i] = columns[i].name;
		}
		VMapLayer elementLayer = new VMapLayer(type, layerColumns);
		
		Collection<NgiRecord<?>> ngiElements = ngiLayer.data.values();
		for(NgiRecord<?> ngiElement : ngiElements) {
			try {
				VMapElement element = fromElement(elementLayer, ngiElement);
				if (element == null) continue;
				elementLayer.add(element);
			} catch(Exception e) {
				KVectorMap2MinecraftMod.logger.error("Error occured while parsing layer " + type + ": " + e.getMessage());
			}
		}
		
		return elementLayer;
	}
	
	
	
	private VMapElement fromElement(VMapLayer layer, NgiRecord<?> ngiElement) throws Exception {
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



	private VMapElement fromMultiPolygon(VMapLayer layer, NgiMultiPolygon polygon) {
		List<Vector2DH[]> vertexList = new ArrayList<>();
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
	
	
	
	private VMapElement fromPolygon(VMapLayer layer, NgiPolygon polygon) {
		Vector2DH[][] vertexList = new Vector2DH[polygon.vertexData.length][];
		
		for(int j = 0; j < polygon.vertexData.length; ++j) {
			int size = polygon.vertexData[j].getSize();
			vertexList[j] = new Vector2DH[size];
			
			for(int i = 0; i < size; ++i) {
				NgiVector vertex = polygon.vertexData[j].getVertex(i);
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
	

	
	private VMapPolyline fromLine(VMapLayer layer, NgiLine line) {
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
	
	
	
	private VMapPoint fromPoint(VMapLayer layer, NgiPoint point) throws Exception {
		Vector2DH vpoint = this.targetProjToWorldProjCoord(point.position.getAxis(0), point.position.getAxis(1));
		
		if(layer.getType() == VMapElementType.표고점) { return new VMapElevationPoint(layer, vpoint, point.rowData); }
		else { return new VMapPoint(layer, vpoint, point.rowData); }
	}
	
}
