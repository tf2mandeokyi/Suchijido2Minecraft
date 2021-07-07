package com.mndk.shapefile.shp;

import com.mndk.shapefile.util.ShapefileCustomInputStream;

import java.io.IOException;
import java.util.Arrays;

public abstract class ShapefileRecord {
	
	
	
	public final int number;
	public final ShapeType type;
	
	
	
	protected ShapefileRecord(int number, ShapeType type) {
		this.number = number;
		this.type = type;
	}
	
	
	
	public static ShapefileRecord from(int recordNumber, int recordLength, ShapefileCustomInputStream is) throws IOException {
		
		ShapefileBoundingBoxXY bbox;
		int numParts, numPoints, i;
		int[] parts;
		ShapeVector[] points;
		ShapeVector[][] polylines;

		int typeId = is.readIntLittle();
		ShapeType type = ShapeType.getType(typeId);
		if(type == null) {
			throw new IOException("Unknown shape type: " + typeId);
		}


		if(type == ShapeType.NULL) return new Null(recordNumber);


		if(type.getParent() == ShapeType.POINT) {
			ShapeVector vector = new ShapeVector(is.readDoubleLittle(), is.readDoubleLittle());
			if(type.containsMeasure) {
				if(type.containsZ) {
					vector.z = is.readDoubleLittle();
				}
				vector.measure = is.readDoubleLittle();
			}
			return new Point(recordNumber, vector);
		}


		if(type.getParent() == ShapeType.MULTIPOINT) {
			bbox = new ShapefileBoundingBoxXY(is);
			numPoints = is.readIntLittle(); points = new ShapeVector[numPoints];
			for(i = 0; i < numPoints; i++) points[i] = new ShapeVector(is);
			if(type.containsMeasure) {
				if(type.containsZ) {
					bbox.readZRange(is);
					for(i = 0; i < numPoints; i++) points[i].z = is.readDoubleLittle();
				}
				bbox.readMeasureRange(is);
				for(i = 0; i < numPoints; i++) points[i].measure = is.readDoubleLittle();
			}
			return new MultiPoint(recordNumber, bbox, points);
		}


		if(type.getParent() == ShapeType.POLYLINE || type.getParent() == ShapeType.POLYGON) {
			bbox = new ShapefileBoundingBoxXY(is);
			numParts = is.readIntLittle(); numPoints = is.readIntLittle();

			parts = new int[numParts]; points = new ShapeVector[numPoints];

			for(i = 0; i < numParts; i++) parts[i] = is.readIntLittle();
			for(i = 0; i < numPoints; i++) points[i] = new ShapeVector(is);

			if(type.containsMeasure) {
				if(type.containsZ) {
					bbox.readZRange(is);
					for(i = 0; i < numPoints; i++) points[i].z = is.readDoubleLittle();
				}
				bbox.readMeasureRange(is);
				for(i = 0; i < numPoints; i++) points[i].measure = is.readDoubleLittle();
			}

			polylines = getPolylinesWithPartsAndPoints(parts, points);

			if(type.getParent() == ShapeType.POLYLINE) {
				return new PolyLine(recordNumber, bbox, polylines);
			}
			else {
				return new Polygon(recordNumber, bbox, polylines);
			}
		}


		if(type == ShapeType.MULTIPATCH) {
			throw new IOException("The shape type \"MULTIPATCH\" is not yet implemented.");
		}


		throw new IOException("Unknown shape type");
	}
	
	
	
	private static ShapeVector[][] getPolylinesWithPartsAndPoints(int[] parts, ShapeVector[] points) {
		ShapeVector[] tempLine;
		ShapeVector[][] result = new ShapeVector[parts.length][];
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
			if (end - start >= 0) System.arraycopy(points, start, tempLine, 0, end - start);
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
		public final ShapeVector vector;
		Point(int number, ShapeVector vector) {
			super(number, ShapeType.POINT);
			this.vector = vector;
		}
		@Override public String toString() {
			return "Point(" + vector + ")";
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
