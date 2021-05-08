package com.mndk.shapefile.shp;

import java.io.IOException;
import java.util.Arrays;

import com.mndk.shapefile.util.ShapefileCustomInputStream;

public abstract class ShapefileRecord {
	
	
	
	public final int number;
	public final ShapeType type;
	
	
	
	protected ShapefileRecord(int number, ShapeType type) {
		this.number = number;
		this.type = type;
	}
	
	
	
	public static ShapefileRecord from(int recordNumber, int recordLength, ShapefileCustomInputStream is) throws IOException {
		
		ShapefileBoundingBoxXY bbox;
		int numParts, numPoints;
		int[] parts;
		ShapeVector[] points, polylines[];
		
		int typeId = is.readIntLittle();
		ShapeType type = ShapeType.getType(typeId);
		if(type == null) {
			throw new IOException("Unknown shape type: " + typeId);
		}
		switch(type) {
			case NULL:
				return new Null(recordNumber);
			case POINT: 
				return new Point(recordNumber, is.readDoubleLittle(), is.readDoubleLittle());
			case MULTIPOINT:
				bbox = new ShapefileBoundingBoxXY(is);
				numPoints = is.readIntLittle(); points = new ShapeVector[numPoints];
				for(int i = 0; i < numPoints; i++) points[i] = new ShapeVector(is);
				return new MultiPoint(recordNumber, bbox, points);
			case POLYLINE:
				bbox = new ShapefileBoundingBoxXY(is);
				numParts = is.readIntLittle();  
				numPoints = is.readIntLittle(); 
				parts = new int[numParts]; for(int j = 0; j < numParts; j++) {
					parts[j] = is.readIntLittle();
				}
				points = new ShapeVector[numPoints]; for(int i = 0; i < numPoints; i++) {
					points[i] = new ShapeVector(is);
				}
				polylines = getPolylinesWithPartsAndPoints(parts, points);
				return new PolyLine(recordNumber, bbox, polylines);
			case POLYGON:
				bbox = new ShapefileBoundingBoxXY(is);
				numParts = is.readIntLittle();  
				numPoints = is.readIntLittle(); 
				parts = new int[numParts]; for(int j = 0; j < numParts; j++) {
					parts[j] = is.readIntLittle();
				}
				points = new ShapeVector[numPoints]; for(int i = 0; i < numPoints; i++) {
					points[i] = new ShapeVector(is);
				}
				polylines = getPolylinesWithPartsAndPoints(parts, points);
				return new Polygon(recordNumber, bbox, polylines);
			case POINTM:
			case MULTIPOINTM:
			case POLYLINEM:
			case POLYGONM:
			case POINTZ:
			case MULTIPOINTZ:
			case POLYLINEZ:
			case POLYGONZ:
			case MULTIPATCH: // TODO finish these
			default:
				throw new IOException("Unknown shape type.");
		}
	}
	
	
	
	private static ShapeVector[][] getPolylinesWithPartsAndPoints(int[] parts, ShapeVector[] points) {
		ShapeVector[] tempLine, result[] = new ShapeVector[parts.length][];
		int start, end;
		for(int j = 0; j < parts.length; j++) {
			start = parts[j];
			if(j != parts.length - 1) {
				end = parts[j + 1];
			}
			else {
				end = points.length;
			}
			result[j] = tempLine = new ShapeVector[end - start];
			for(int i = start; i < end; i++) {
				tempLine[i - start] = points[i];
			}
		}
		return result;
	}
	
	
	
	public static class Null extends ShapefileRecord {
		Null(int number) {
			super(number, ShapeType.NULL);
		}
		@Override public String toString() {
			return "NullShape";
		}
	}
	
	
	
	public static class Point extends ShapefileRecord {
		public final double x, y;
		Point(int number, double x, double y) {
			super(number, ShapeType.POINT);
			this.x = x;
			this.y = y;
		}
		@Override public String toString() {
			return "Point(" + x + "," + y + ")";
		}
	}
	
	
	
	public static class MultiPoint extends ShapefileRecord {
		public final ShapefileBoundingBoxXY bbox;
		public final ShapeVector[] points;
		MultiPoint(int number, ShapefileBoundingBoxXY bbox, ShapeVector[] points) {
			super(number, ShapeType.MULTIPOINT);
			this.bbox = bbox;
			this.points = points;
		}
		@Override public String toString() {
			return "MultiPoint" + Arrays.toString(points);
		}
	}
	
	
	
	public static class PolyLine extends ShapefileRecord {
		public final ShapefileBoundingBoxXY bbox;
		public final ShapeVector[][] points;
		PolyLine(int number, ShapefileBoundingBoxXY bbox, ShapeVector[][] points) {
			super(number, ShapeType.POLYLINE);
			this.bbox = bbox;
			this.points = points;
		}
		@Override public String toString() {
			return "PolyLine" + Arrays.deepToString(points);
		}
	}
	
	
	
	public static class Polygon extends ShapefileRecord {
		public final ShapefileBoundingBoxXY bbox;
		public final ShapeVector[][] points;
		Polygon(int number, ShapefileBoundingBoxXY bbox, ShapeVector[][] points) {
			super(number, ShapeType.POLYGON);
			this.bbox = bbox;
			this.points = points;
		}
		@Override public String toString() {
			return "Polygon" + Arrays.deepToString(points);
		}
	}
}
