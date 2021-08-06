package com.mndk.scjd2mc.core.scjd.reader;

import com.mndk.ngiparser.NgiParser;
import com.mndk.ngiparser.ngi.NgiLayer;
import com.mndk.ngiparser.ngi.NgiParserResult;
import com.mndk.ngiparser.ngi.element.*;
import com.mndk.ngiparser.ngi.vertex.NgiVector;
import com.mndk.scjd2mc.core.db.common.TableColumn;
import com.mndk.scjd2mc.core.db.common.TableColumns;
import com.mndk.scjd2mc.core.scjd.SuchijidoData;
import com.mndk.scjd2mc.core.scjd.SuchijidoUtils;
import com.mndk.scjd2mc.core.scjd.elem.ScjdLayer;
import com.mndk.scjd2mc.core.scjd.geometry.*;
import com.mndk.scjd2mc.core.scjd.type.ElementDataType;
import com.mndk.scjd2mc.core.util.math.Vector2DH;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

public class NgiDataFileReader extends SuchijidoFileReader {


	@Override
	protected SuchijidoData getResult() throws IOException {

		SuchijidoData result = new SuchijidoData();

		NgiParserResult parseResult = NgiParser.parse(mapFile.getAbsolutePath(), "MS949", true);
		
		Collection<NgiLayer> layers = parseResult.getLayers().values();

		for(NgiLayer layer : layers) {
			if(layer.header.dimensions != 2) continue;
			try {
				ElementDataType type = ElementDataType.fromLayerName(layer.name);
				TableColumns columns = type.getColumns();
				ScjdLayer scjdLayer = result.getLayer(type);

				Collection<NgiRecord<?>> ngiElements = layer.data.values();

				for(NgiRecord<?> ngiElement : ngiElements) {

					GeometryShape<?> geometryRecord = fromNgiRecord(ngiElement);
					if(geometryRecord == null) continue;

					Object[] dataRow = new Object[columns.getLength()];
					for(int i = 0; i < columns.getLength(); ++i) {
						TableColumn column = columns.get(i);
						dataRow[i] = ngiElement.getRowData(column.getName());
					}

					scjdLayer.add(SuchijidoUtils.combineGeometryAndData(
							scjdLayer, geometryRecord, type, dataRow, UUID.randomUUID().toString(), options));

				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		return result;
	}



	@Nullable
	private GeometryShape<?> fromNgiRecord(NgiRecord<?> ngiElement) throws Exception {

		if(ngiElement instanceof NgiMultiPolygon) {
			return multiPolygon((NgiMultiPolygon) ngiElement);
		}
		else if(ngiElement instanceof NgiPolygon) {
			return polygon((NgiPolygon) ngiElement);
		}
		else if(ngiElement instanceof NgiLine) {
			return line((NgiLine) ngiElement);
		}
		else if(ngiElement instanceof NgiPoint) {
			return point((NgiPoint) ngiElement);
		}
		else if(ngiElement instanceof NgiText) {
			return null;
		}
		else throw new Exception("Invalid record type: " + ngiElement.getClass().getName());
	}



	private MultiPolygon multiPolygon(NgiMultiPolygon multiPolygon) {
		Polygon[] polygons = new Polygon[multiPolygon.vertexData.length];

		for(int k = 0; k < multiPolygon.vertexData.length; ++k) {

			int polygonSize = multiPolygon.vertexData[k].length;
			LineString[] lineStrings = new LineString[polygonSize];

			for (int j = 0; j < polygonSize; ++j) {

				int lineSize = multiPolygon.vertexData[k][j].getSize();
				Vector2DH[] points = new Vector2DH[lineSize];

				for (int i = 0; i < lineSize; ++i) {
					NgiVector vertex = multiPolygon.vertexData[k][j].getVertex(i);
					points[i] = this.targetProjToWorldProjCoord(vertex.getAxis(0), vertex.getAxis(1));
				}

				lineStrings[j] = new LineString(points);
			}
			polygons[k] = new Polygon(lineStrings);
		}

		return new MultiPolygon(polygons);
	}
	
	
	
	private Polygon polygon(NgiPolygon polygon) {

		int polygonSize = polygon.vertexData.length;
		LineString[] lineStrings = new LineString[polygonSize];
		
		for(int j = 0; j < polygonSize; ++j) {

			int lineSize = polygon.vertexData[j].getSize();
			Vector2DH[] points = new Vector2DH[lineSize];
			
			for(int i = 0; i < lineSize; ++i) {
				NgiVector vertex = polygon.vertexData[j].getVertex(i);
				points[i] = this.targetProjToWorldProjCoord(vertex.getAxis(0), vertex.getAxis(1));
			}

			lineStrings[j] = new LineString(points);
		}

		return new Polygon(lineStrings);
	}
	

	
	private LineString line(NgiLine line) {
		int lineSize = line.lineData.getSize();
		Vector2DH[] points = new Vector2DH[lineSize];
		
		for(int i = 0; i < lineSize; ++i) {
			NgiVector vertex = line.lineData.getVertex(i);
			points[i] = this.targetProjToWorldProjCoord(vertex.getAxis(0), vertex.getAxis(1));
		}

		return new LineString(points);
	}
	
	
	
	private Point point(NgiPoint point) {
		return new Point(
				this.targetProjToWorldProjCoord(point.position.getAxis(0), point.position.getAxis(1)));
	}
	
}
