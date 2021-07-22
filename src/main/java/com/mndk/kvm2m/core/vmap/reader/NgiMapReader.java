package com.mndk.kvm2m.core.vmap.reader;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vmap.*;
import com.mndk.kvm2m.core.db.common.TableColumn;
import com.mndk.kvm2m.core.db.common.TableColumns;
import com.mndk.ngiparser.NgiParser;
import com.mndk.ngiparser.ngi.NgiLayer;
import com.mndk.ngiparser.ngi.NgiParserResult;
import com.mndk.ngiparser.ngi.element.*;
import com.mndk.ngiparser.ngi.vertex.NgiVector;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public class NgiMapReader extends VMapReader {


	@Override
	protected Map.Entry<VMapPayload.Geometry, VMapPayload.Data> getResult() throws IOException {

		VMapPayload.Geometry geometryPayload = new VMapPayload.Geometry();
		VMapPayload.Data dataPayload = new VMapPayload.Data();

		NgiParserResult parseResult = NgiParser.parse(mapFile.getAbsolutePath(), "MS949", true);
		
		Collection<NgiLayer> layers = parseResult.getLayers().values();

		long count = 0;

		for(NgiLayer layer : layers) {
			if(layer.header.dimensions != 2) continue;
			try {
				VMapElementDataType type = VMapElementDataType.fromLayerName(layer.name);
				TableColumns columns = type.getColumns();

				Collection<NgiRecord<?>> ngiElements = layer.data.values();

				for(NgiRecord<?> ngiElement : ngiElements) {

					VMapPayload.Geometry.Record<?> geometryRecord = fromNgiRecord(ngiElement);
					if(geometryRecord == null) continue;

					Object[] dataRow = new Object[columns.getLength()];
					for(int i = 0; i < columns.getLength(); ++i) {
						TableColumn column = columns.get(i);
						dataRow[i] = ngiElement.getRowData(column.getCategoryName());
					}

					VMapPayload.Data.Record dataRecord = new VMapPayload.Data.Record(type, dataRow);

					geometryPayload.put(count, geometryRecord);
					dataPayload.put(count, dataRecord);
					++count;
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return new AbstractMap.SimpleEntry<>(geometryPayload, dataPayload);
		
	}



	@Nullable
	private VMapPayload.Geometry.Record<?> fromNgiRecord(NgiRecord<?> ngiElement) throws Exception {

		if(ngiElement instanceof NgiMultiPolygon) {
			return new VMapPayload.Geometry.Record<>(
					VMapElementGeomType.POLYGON, fromMultiPolygon((NgiMultiPolygon) ngiElement));
		}
		else if(ngiElement instanceof NgiPolygon) {
			return new VMapPayload.Geometry.Record<>(
					VMapElementGeomType.POLYGON, fromPolygon((NgiPolygon) ngiElement));
		}
		else if(ngiElement instanceof NgiLine) {
			return new VMapPayload.Geometry.Record<>(
					VMapElementGeomType.LINESTRING, fromLine((NgiLine) ngiElement));
		}
		else if(ngiElement instanceof NgiPoint) {
			return new VMapPayload.Geometry.Record<>(
					VMapElementGeomType.POINT, fromPoint((NgiPoint) ngiElement));
		}
		else if(ngiElement instanceof NgiText) {
			return null;
		}
		else throw new Exception("Invalid record type: " + ngiElement.getClass().getName());
	}



	private Vector2DH[][] fromMultiPolygon(NgiMultiPolygon polygon) {
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

		return vertexList.toArray(new Vector2DH[0][]);
	}
	
	
	
	private Vector2DH[][] fromPolygon(NgiPolygon polygon) {
		Vector2DH[][] vertexList = new Vector2DH[polygon.vertexData.length][];
		
		for(int j = 0; j < polygon.vertexData.length; ++j) {
			int size = polygon.vertexData[j].getSize();
			vertexList[j] = new Vector2DH[size];
			
			for(int i = 0; i < size; ++i) {
				NgiVector vertex = polygon.vertexData[j].getVertex(i);
				vertexList[j][i] = this.targetProjToWorldProjCoord(vertex.getAxis(0), vertex.getAxis(1));
			}
		}

		return vertexList;
	}
	

	
	private Vector2DH[][] fromLine(NgiLine line) {
		int size = line.lineData.getSize();
		Vector2DH[] vertexList = new Vector2DH[size];
		
		for(int i = 0; i < size; ++i) {
			NgiVector vertex = line.lineData.getVertex(i);
			vertexList[i] = this.targetProjToWorldProjCoord(vertex.getAxis(0), vertex.getAxis(1));
		}

		return new Vector2DH[][] { vertexList };
	}
	
	
	
	private Vector2DH fromPoint(NgiPoint point) {
		return this.targetProjToWorldProjCoord(point.position.getAxis(0), point.position.getAxis(1));
	}
	
}
