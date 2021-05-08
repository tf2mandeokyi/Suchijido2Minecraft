package com.mndk.shapefile.shp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import com.mndk.shapefile.util.Endian;

public abstract class ShpRecord {
	
	
	
	public final int number;
	public final ShapeType type;
	
	
	
	protected ShpRecord(int number, ShapeType type) {
		this.number = number;
		this.type = type;
	}
	
	
	
	public static ShpRecord from(int recordNumber, int recordLength, InputStream is) throws IOException {
		
		ShpBoundingBoxXY bbox;
		int numParts, numPoints;
		int[] parts;
		ShpVector[] points, polylines[];
		
		ShapeType type = ShapeType.getType(Endian.readIntegerLittle(is));
		switch(type) {
			case NULL:
				return new Null(recordNumber);
			case POINT: 
				return new Point(recordNumber, Endian.readDoubleLittle(is), Endian.readDoubleLittle(is));
			case MULTIPOINT:
				bbox = new ShpBoundingBoxXY(is);
				numPoints = Endian.readIntegerLittle(is); points = new ShpVector[numPoints];
				for(int i = 0; i < numPoints; i++) points[i] = new ShpVector(is);
				return new MultiPoint(recordNumber, bbox, points);
			case POLYLINE:
				bbox = new ShpBoundingBoxXY(is);
				numParts = Endian.readIntegerLittle(is);  
				numPoints = Endian.readIntegerLittle(is); 
				parts = new int[numParts]; for(int j = 0; j < numParts; j++) {
					parts[j] = Endian.readIntegerLittle(is);
				}
				points = new ShpVector[numPoints]; for(int i = 0; i < numPoints; i++) {
					points[i] = new ShpVector(is);
				}
				polylines = getPolylinesWithPartsAndPoints(parts, points);
				return new PolyLine(recordNumber, bbox, polylines);
			case POLYGON:
				bbox = new ShpBoundingBoxXY(is);
				numParts = Endian.readIntegerLittle(is);  
				numPoints = Endian.readIntegerLittle(is); 
				parts = new int[numParts]; for(int j = 0; j < numParts; j++) {
					parts[j] = Endian.readIntegerLittle(is);
				}
				points = new ShpVector[numPoints]; for(int i = 0; i < numPoints; i++) {
					points[i] = new ShpVector(is);
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
	
	
	
	private static ShpVector[][] getPolylinesWithPartsAndPoints(int[] parts, ShpVector[] points) {
		ShpVector[] tempLine, result[] = new ShpVector[parts.length][];
		int start, end;
		for(int j = 0; j < parts.length; j++) {
			start = parts[j];
			if(j != parts.length - 1) {
				end = parts[j + 1];
			}
			else {
				end = points.length;
			}
			result[j] = tempLine = new ShpVector[end - start];
			for(int i = start; i < end; i++) {
				tempLine[i - start] = points[i];
			}
		}
		return result;
	}
	
	
	
	public static class Null extends ShpRecord {
		Null(int number) {
			super(number, ShapeType.NULL);
		}
		@Override public String toString() {
			return "NullShape";
		}
	}
	
	
	
	public static class Point extends ShpRecord {
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
	
	
	
	public static class MultiPoint extends ShpRecord {
		public final ShpBoundingBoxXY bbox;
		public final ShpVector[] points;
		MultiPoint(int number, ShpBoundingBoxXY bbox, ShpVector[] points) {
			super(number, ShapeType.MULTIPOINT);
			this.bbox = bbox;
			this.points = points;
		}
		@Override public String toString() {
			return "MultiPoint" + Arrays.toString(points);
		}
	}
	
	
	
	public static class PolyLine extends ShpRecord {
		public final ShpBoundingBoxXY bbox;
		public final ShpVector[][] points;
		PolyLine(int number, ShpBoundingBoxXY bbox, ShpVector[][] points) {
			super(number, ShapeType.POLYLINE);
			this.bbox = bbox;
			this.points = points;
		}
		@Override public String toString() {
			return "PolyLine" + Arrays.deepToString(points);
		}
	}
	
	
	
	public static class Polygon extends ShpRecord {
		public final ShpBoundingBoxXY bbox;
		public final ShpVector[][] points;
		Polygon(int number, ShpBoundingBoxXY bbox, ShpVector[][] points) {
			super(number, ShapeType.POLYGON);
			this.bbox = bbox;
			this.points = points;
		}
		@Override public String toString() {
			return "Polygon" + Arrays.deepToString(points);
		}
	}
}
