package com.mndk.scjd2mc.core.scjd.reader;

import com.mndk.scjd2mc.core.db.common.TableColumn;
import com.mndk.scjd2mc.core.db.common.TableColumns;
import com.mndk.scjd2mc.core.scjd.ScjdDataPayload;
import com.mndk.scjd2mc.core.scjd.geometry.*;
import com.mndk.scjd2mc.core.scjd.type.ElementDataType;
import com.mndk.scjd2mc.core.util.file.DirectoryManager;
import com.mndk.scjd2mc.core.util.file.ZipManager;
import com.mndk.scjd2mc.core.util.math.Vector2DH;
import com.mndk.shapefile.ShpDbfDataIterator;
import com.mndk.shapefile.ShpDbfRecord;
import com.mndk.shapefile.shp.ShapeVector;
import com.mndk.shapefile.shp.ShapefileRecord;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.AbstractMap;
import java.util.Map;


public class ShpZipDataFileReader extends SuchijidoFileReader {


	@Override
	protected Map.Entry<ScjdDataPayload.Geometry, ScjdDataPayload.Data> getResult() throws IOException {

		ScjdDataPayload.Geometry geometryPayload = new ScjdDataPayload.Geometry();
		ScjdDataPayload.Data dataPayload = new ScjdDataPayload.Data();
		
		Throwable throwable = null;

		String mapFilePath = mapFile.getAbsolutePath();
		File zipDestination = new File(mapFilePath.substring(0, mapFilePath.lastIndexOf(".zip")) + "/");

		try {

			if(zipDestination.exists() && !zipDestination.delete()) {
				throw new IOException("Failed to delete files");
			}

			// Extract all files in map file
			if(!zipDestination.mkdir()) {
				throw new IOException("Failed to create directory");
			}
			ZipManager.extractZipFile(mapFile, zipDestination, "cp949");

			File[] shapeFiles = zipDestination.listFiles((dir, name) -> name.endsWith(".shp"));
			assert shapeFiles != null;

			if(shapeFiles.length == 0) {
				System.err.println("Huh? I can't find any .shp file in this folder! >:(");
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

						GeometryShape<?> geometryRecord = fromShpRecord(record.shape);

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



	protected GeometryShape<?> fromShpRecord(ShapefileRecord record) {
		if(record instanceof ShapefileRecord.Polygon) {
			return polygon((ShapefileRecord.Polygon) record);
		}
		else if(record instanceof ShapefileRecord.PolyLine) {
			return line((ShapefileRecord.PolyLine) record);
		}
		else if(record instanceof ShapefileRecord.Point) {
			return point((ShapefileRecord.Point) record);
		}
		return null;
	}
	
	
	
	private Polygon polygon(ShapefileRecord.Polygon polygon) {
		ShapeVector[][] points = polygon.points;
		int polygonSize = points.length;
		LineString[] lineStrings = new LineString[polygonSize];
		
		for(int j = 0; j < polygonSize; ++j) {
			int lineSize = points[j].length;
			Vector2DH[] linePoints = new Vector2DH[lineSize];
			
			for(int i = 0; i < lineSize; ++i) {
				ShapeVector vector = points[j][i];
				linePoints[i] = this.targetProjToWorldProjCoord(vector.x, vector.y);
			}

			lineStrings[j] = new LineString(linePoints);
		}

		return new Polygon(lineStrings);
	}
	

	
	private GeometryShape<?> line(ShapefileRecord.PolyLine polyline) {
		ShapeVector[][] points = polyline.points;
		int lineCount = polyline.points.length;

		if(lineCount == 0) {
			return null;
		}
		else if(lineCount == 1) {

			int lineSize = points[0].length;
			Vector2DH[] linePoints = new Vector2DH[lineSize];

			for(int i = 0; i < lineSize; ++i) {
				ShapeVector vector = points[0][i];
				linePoints[i] = this.targetProjToWorldProjCoord(vector.x, vector.y);
			}

			return new LineString(linePoints);

		} else {

			LineString[] result = new LineString[lineCount];

			for(int j = 0; j < points.length; ++j) {
				int lineSize = points[j].length;
				Vector2DH[] linePoints = new Vector2DH[lineSize];

				for(int i = 0; i < lineSize; ++i) {
					ShapeVector vector = points[j][i];
					linePoints[i] = this.targetProjToWorldProjCoord(vector.x, vector.y);
				}

				result[j] = new LineString(linePoints);
			}

			return new MultiLineString(result);
		}
	}
	
	
	
	private Point point(ShapefileRecord.Point point) {
		return new Point(this.targetProjToWorldProjCoord(point.vector.x, point.vector.y));
	}

}
