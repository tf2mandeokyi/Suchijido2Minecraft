package com.mndk.scjd2mc.core.scjd.reader;

import com.mndk.scjd2mc.core.util.file.DirectoryManager;
import com.mndk.scjd2mc.core.util.file.ZipManager;
import com.mndk.scjd2mc.core.util.math.Vector2DH;
import com.mndk.scjd2mc.core.scjd.*;
import com.mndk.scjd2mc.core.db.common.TableColumn;
import com.mndk.scjd2mc.core.db.common.TableColumns;
import com.mndk.scjd2mc.core.scjd.type.ElementDataType;
import com.mndk.scjd2mc.core.scjd.type.ElementGeometryType;
import com.mndk.shapefile.ShpDbfDataIterator;
import com.mndk.shapefile.ShpDbfRecord;
import com.mndk.shapefile.shp.ShapeVector;
import com.mndk.shapefile.shp.ShapefileRecord;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.AbstractMap;
import java.util.Map;


public class ShpZipMapReader extends VMapReader {


	@Override
	protected Map.Entry<ScjdDataPayload.Geometry, ScjdDataPayload.Data> getResult() throws IOException {

		ScjdDataPayload.Geometry geometryPayload = new ScjdDataPayload.Geometry();
		ScjdDataPayload.Data dataPayload = new ScjdDataPayload.Data();
		
		Throwable throwable = null;

		String mapFilePath = mapFile.getAbsolutePath();
		File zipDestination = new File(mapFilePath.substring(0, mapFilePath.lastIndexOf(".zip")) + "/");

		try {

			if(zipDestination.exists()) {
				zipDestination.delete();
			}

			// Extract all files in map file
			zipDestination.mkdir();
			ZipManager.extractZipFile(mapFile, zipDestination, "cp949");

			File[] shapeFiles = zipDestination.listFiles((dir, name) -> name.endsWith(".shp"));
			assert shapeFiles != null;

			if(shapeFiles.length == 0) {
				System.err.println("Huh? I can't found any .shp file in this folder! >:(");
			}

			long count = 0;

			for(File shapeFile : shapeFiles) {
				String filePath = shapeFile.getAbsolutePath();
				filePath = filePath.substring(0, filePath.length() - 4);
				String fileName = new File(filePath).getName();
				ElementDataType type = ElementDataType.fromLayerName(fileName);
				TableColumns columns = type.getColumns();

				try(ShpDbfDataIterator iterator =
							new ShpDbfDataIterator(filePath, Charset.forName("cp949"))) {

					for (ShpDbfRecord record : iterator) {
						// System.out.println(record.dBase);

						ScjdDataPayload.Geometry.Record<?> geometryRecord = fromShpRecord(record.shape);

						Object[] dataRow = new Object[columns.getLength()];
						for(int i = 0; i < columns.getLength(); ++i) {
							TableColumn column = columns.get(i);
							dataRow[i] = record.dBase.getDataByField(column.getName());
						}

						ScjdDataPayload.Data.Record dataRecord = new ScjdDataPayload.Data.Record(type, dataRow);

						geometryPayload.put(count, geometryRecord);
						dataPayload.put(count, dataRecord);
						++count;
					}
				}
			}
		} catch(Throwable t) {
			throwable = t;
		}

		DirectoryManager.deleteDirectory(zipDestination);
		
		if(throwable != null) {
			if(throwable instanceof IOException) {
				throw (IOException) throwable;
			}
			else {
				throw new RuntimeException(throwable);
			}
		}

		return new AbstractMap.SimpleEntry<>(geometryPayload, dataPayload);
		
	}



	protected ScjdDataPayload.Geometry.Record<?> fromShpRecord(ShapefileRecord record) {
		if(record instanceof ShapefileRecord.Polygon) {
			return new ScjdDataPayload.Geometry.Record<>(
					ElementGeometryType.POLYGON, fromPolygon((ShapefileRecord.Polygon) record));
		}
		else if(record instanceof ShapefileRecord.PolyLine) {
			return new ScjdDataPayload.Geometry.Record<>(
					ElementGeometryType.LINESTRING, fromLine((ShapefileRecord.PolyLine) record));
		}
		else if(record instanceof ShapefileRecord.Point) {
			return new ScjdDataPayload.Geometry.Record<>(
					ElementGeometryType.POINT, fromPoint((ShapefileRecord.Point) record));
		}
		return null;
	}
	
	
	
	private Vector2DH[][] fromPolygon(ShapefileRecord.Polygon polygon) {
		ShapeVector[][] points = polygon.points;
		Vector2DH[][] vertexList = new Vector2DH[polygon.points.length][];
		
		for(int j = 0; j < points.length; ++j) {
			int size = points[j].length;
			vertexList[j] = new Vector2DH[size];
			
			for(int i = 0; i < size; ++i) {
				ShapeVector vector = points[j][i];
				vertexList[j][i] = this.targetProjToWorldProjCoord(vector.x, vector.y);
			}
		}

		return vertexList;
	}
	

	
	private Vector2DH[][] fromLine(ShapefileRecord.PolyLine polyline) {
		ShapeVector[][] points = polyline.points;
		Vector2DH[][] vertexList = new Vector2DH[polyline.points.length][];
		
		for(int j = 0; j < points.length; ++j) {
			int size = points[j].length;
			vertexList[j] = new Vector2DH[size];
			
			for(int i = 0; i < size; ++i) {
				ShapeVector vector = points[j][i];
				vertexList[j][i] = this.targetProjToWorldProjCoord(vector.x, vector.y);
			}
		}

		return vertexList;
	}
	
	
	
	private Vector2DH fromPoint(ShapefileRecord.Point point) {
		return this.targetProjToWorldProjCoord(point.vector.x, point.vector.y);
	}

}
