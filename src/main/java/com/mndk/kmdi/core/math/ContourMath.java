package com.mndk.kmdi.core.math;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.mndk.kmdi.core.dxfmap.elem.polyline.DXFMapContour;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;

public class ContourMath {
	
	static Map.Entry<DXFMapContour, Vector> getClosestContour(Vector2D p, List<DXFMapContour> contourList) {
		
		double closestDistance = Double.MAX_VALUE;
		DXFMapContour closestContour = null;
		Vector closestPoint = null;
		Map.Entry<Vector2D, Double> tempEntry;
		
		for(DXFMapContour contour : contourList) {
			tempEntry = contour.getClosestPointToPoint(p);
			if(closestDistance > tempEntry.getValue()) {
				closestDistance = tempEntry.getValue();
				closestContour = contour;
				closestPoint = tempEntry.getKey().toVector(contour.getElevation());
			}
		}
		return new AbstractMap.SimpleEntry<>(closestContour, closestPoint);
	}
	
	
	
	static List<Vector> get4LineIntersectionPoints(List<DXFMapContour> contourList, Vector2D sLineP0, Vector2D sLinePDelta) {
		
		List<Map.Entry<Vector, Double>> intersections = new ArrayList<>();
		List<Vector> result = new ArrayList<>();
		
		for(DXFMapContour contour : contourList) {
			intersections.addAll(contour.getStraightLineIntersections(sLineP0, sLinePDelta));
		}
		
		if(intersections.size() > 4) {
			intersections.sort((e1, e2) -> Double.compare(e1.getValue(), e2.getValue()));
			intersections = intersections.subList(0, 4);
		}
		
		intersections.forEach(entry -> {
			result.add(entry.getKey());
		});
		
		return result;
	}
	
	
	
	public static double getPointHeightFromContourList(Vector2D point, List<DXFMapContour> contourList) {
		
		Vector closestPoint = getClosestContour(point, contourList).getValue();
		Vector2D closestPoint2D = closestPoint.toVector2D(), d = closestPoint2D.subtract(point);
		
		if(d.length() < 0.001) return closestPoint.getY();
		
		Vector2D[] rayDeltaPoints = new Vector2D[] {
				d,
				new Vector2D(d.getX()+d.getZ(), d.getZ()-d.getX()),
				new Vector2D(d.getZ(), -d.getX()),
				new Vector2D(d.getZ()-d.getX(), -d.getX()-d.getZ())
		};
		
		List<Vector> contourPoints = new ArrayList<Vector>();
		
		for(Vector2D rayDelta : rayDeltaPoints) {
			List<Vector> tmpList = get4LineIntersectionPoints(contourList, point, rayDelta);
			contourPoints.addAll(tmpList);
		}
		
		return SplineMath.getHeight(closestPoint2D, contourPoints.toArray(new Vector[0]));
	}
	
	public static void main(String[] args) {
		List<DXFMapContour> list = Arrays.asList(new DXFMapContour[] {
				new DXFMapContour(new Vector2D[] {new Vector2D(3, 3), new Vector2D(3, -3), new Vector2D(-3, -3), new Vector2D(-3, 3), new Vector2D(3, 3)}, 10),
				new DXFMapContour(new Vector2D[] {new Vector2D(6, 6), new Vector2D(6, -6), new Vector2D(-6, -6), new Vector2D(-6, 6), new Vector2D(6, 6)}, 7),
				new DXFMapContour(new Vector2D[] {new Vector2D(9, 9), new Vector2D(9, -9), new Vector2D(-9, -9), new Vector2D(-9, 9), new Vector2D(9, 9)}, 2)
		});
		for(int i=30;i<=100;i++) {
			System.out.printf("%6.1f | ", i/10.);
			double height = getPointHeightFromContourList(new Vector2D(0, (i/10.)), list);
			for(int j=0;j<Math.round(height*10);j++) {
				System.out.print(j%10==9?"@":"#");
			}
			System.out.println();
		}
	}
}
