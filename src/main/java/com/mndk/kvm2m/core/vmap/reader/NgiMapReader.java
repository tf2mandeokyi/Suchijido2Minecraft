package com.mndk.kvm2m.core.vmap.reader;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vmap.VMapDataPayload;
import com.mndk.kvm2m.core.vmap.VMapElementDataType;
import com.mndk.kvm2m.core.vmap.VMapElementGeomType;
import com.mndk.kvm2m.core.vmap.VMapGeometryPayload;
import com.mndk.kvm2m.db.common.TableColumn;
import com.mndk.kvm2m.db.common.TableColumns;
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
	protected Map.Entry<VMapGeometryPayload, VMapDataPayload> getResult() throws IOException {

		VMapGeometryPayload geometryPayload = new VMapGeometryPayload();
		VMapDataPayload dataPayload = new VMapDataPayload();

		NgiParserResult parseResult = NgiParser.parse(mapFile.getAbsolutePath(), "MS949", true);
		
		Collection<NgiLayer> layers = parseResult.getLayers().values();

		int count = 0;

		for(NgiLayer layer : layers) {
			if(layer.header.dimensions != 2) continue;
			try {
				VMapElementDataType type = VMapElementDataType.fromLayerName(layer.name);
				TableColumns columns = type.getColumns();

				Collection<NgiRecord<?>> ngiElements = layer.data.values();

				for(NgiRecord<?> ngiElement : ngiElements) {

					VMapGeometryPayload.Record<?> geometryRecord = fromNgiRecord(ngiElement);
					if(geometryRecord == null) continue;

					Object[] dataRow = new Object[columns.getLength()];
					for(int i = 0; i < columns.getLength(); ++i) {
						TableColumn column = columns.get(i);
						dataRow[i] = ngiElement.getRowData(column.getCategoryName());
					}

					VMapDataPayload.Record dataRecord = new VMapDataPayload.Record(type, dataRow);

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
	private VMapGeometryPayload.Record<?> fromNgiRecord(NgiRecord<?> ngiElement) throws Exception {

		if(ngiElement instanceof NgiMultiPolygon) {
			return new VMapGeometryPayload.Record<>(
					VMapElementGeomType.POLYGON, fromMultiPolygon((NgiMultiPolygon) ngiElement));
		}
		else if(ngiElement instanceof NgiPolygon) {
			return new VMapGeometryPayload.Record<>(
					VMapElementGeomType.POLYGON, fromPolygon((NgiPolygon) ngiElement));
		}
		else if(ngiElement instanceof NgiLine) {
			return new VMapGeometryPayload.Record<>(
					VMapElementGeomType.LINESTRING, fromLine((NgiLine) ngiElement));
		}
		else if(ngiElement instanceof NgiPoint) {
			return new VMapGeometryPayload.Record<>(
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
