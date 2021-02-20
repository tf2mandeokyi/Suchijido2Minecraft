package com.mndk.kmdi.core.math;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mndk.kmdi.core.dxfmap.elem.polyline.DXFMapContour;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;

public class ContourMath {
	
	static Map.Entry<DXFMapContour, Vector2D> getClosestContour(Vector2D p, List<DXFMapContour> contourList) {
		
		double closestDistance = Double.MAX_VALUE;
		DXFMapContour closestContour = null;
		Vector2D closestPoint = null;
		Map.Entry<Vector2D, Double> tempEntry;
		
		for(DXFMapContour contour : contourList) {
			tempEntry = contour.getClosestPointToPoint(p);
			if(closestDistance > tempEntry.getValue()) {
				closestDistance = tempEntry.getValue();
				closestContour = contour;
				closestPoint = tempEntry.getKey();
			}
		}
		return new AbstractMap.SimpleEntry<>(closestContour, closestPoint);
	}
	
	
	static Vector[] get2IntersectionPointsFromContourList(Vector2D rayStart, Vector2D rayDelta, List<DXFMapContour> contourList) {
		List<Map.Entry<Vector, Double>> intersections = new ArrayList<>();
		for(DXFMapContour contour : contourList) {
			intersections.addAll(contour.getRayIntersections(rayStart, rayDelta));
		}
		Vector closest = null, secondClosest = null;
		double closestDistance = Double.MAX_VALUE, secondClosestDistance = Double.MAX_VALUE;
		for(Map.Entry<Vector, Double> entry : intersections) {
			double distance = entry.getValue();
			if(distance < closestDistance) {
				secondClosest = closest;
				secondClosestDistance = closestDistance;
				closest = entry.getKey();
				closestDistance = distance;
			}
			else if(distance < secondClosestDistance) {
				secondClosest = entry.getKey();
				secondClosestDistance = entry.getValue();
			}
		}
		return new Vector[] {
			closest, secondClosest
		};
	}
	
	
	
	
	public static double getPointHeightFromContourList(Vector2D point, List<DXFMapContour> contourList) {
		double totalSum = 0; int successfulCalculationCount = 0;
		
		Vector2D closestPoint = getClosestContour(point, contourList).getValue();
		Vector2D d = closestPoint.subtract(point);
		
		Vector2D[] rayDeltaPoints = new Vector2D[] {
				d,
				new Vector2D(d.getX()+d.getZ(), d.getZ()-d.getX()),
				new Vector2D(d.getZ(), -d.getX()),
				new Vector2D(d.getZ()-d.getX(), -d.getX()-d.getZ())
		};
		
		for(Vector2D rayDelta : rayDeltaPoints) {
			// System.out.println(point + " -> " + rayDelta);
			Vector[] forwards = get2IntersectionPointsFromContourList(point, rayDelta, contourList);
			Vector[] backwards = get2IntersectionPointsFromContourList(point, Vector2D.ZERO.subtract(rayDelta), contourList);
			
			if(forwards[0] == null) {
				// No intersection
				continue;
			}
			else if(forwards[1] == null) {
				if(backwards[0] == null) return forwards[0].getY();
				forwards[1] = new Vector(Double.MAX_VALUE, forwards[0].getY(), Double.MAX_VALUE);
			}
			
			if(backwards[0] == null) {
				// Intersection found, but is not enough to calculate
				continue;
				// backwards[0] = backwards[1] = forwards[0];
			}
			else if(backwards[1] == null) {
				backwards[1] = new Vector(Double.MAX_VALUE, backwards[0].getY(), Double.MAX_VALUE);
			}
			
			double heightResult = SplineMath.getHeight(0, 
					new Vector2D(-Math.sqrt((point.getX()-backwards[1].getX())*(point.getX()-backwards[1].getX()) + (point.getZ()-backwards[1].getZ())*(point.getZ()-backwards[1].getZ())), backwards[1].getY()),
					new Vector2D(-Math.sqrt((point.getX()-backwards[0].getX())*(point.getX()-backwards[0].getX()) + (point.getZ()-backwards[0].getZ())*(point.getZ()-backwards[0].getZ())), backwards[0].getY()),
					new Vector2D(Math.sqrt((point.getX()-forwards[0].getX())*(point.getX()-forwards[0].getX()) + (point.getZ()-forwards[0].getZ())*(point.getZ()-forwards[0].getZ())), forwards[0].getY()),
					new Vector2D(Math.sqrt((point.getX()-forwards[1].getX())*(point.getX()-forwards[1].getX()) + (point.getZ()-forwards[1].getZ())*(point.getZ()-forwards[1].getZ())), forwards[1].getY())
			);
			/*System.out.println(" - " + new Vector2D(-Math.sqrt((point.getX()-backwards[1].getX())*(point.getX()-backwards[1].getX()) + (point.getZ()-backwards[1].getZ())*(point.getZ()-backwards[1].getZ())), backwards[1].getY()) + ", " +
					new Vector2D(-Math.sqrt((point.getX()-backwards[0].getX())*(point.getX()-backwards[0].getX()) + (point.getZ()-backwards[0].getZ())*(point.getZ()-backwards[0].getZ())), backwards[0].getY()) + ", " +
					new Vector2D(Math.sqrt((point.getX()-forwards[0].getX())*(point.getX()-forwards[0].getX()) + (point.getZ()-forwards[0].getZ())*(point.getZ()-forwards[0].getZ())), forwards[0].getY()) + ", " +
					new Vector2D(Math.sqrt((point.getX()-forwards[1].getX())*(point.getX()-forwards[1].getX()) + (point.getZ()-forwards[1].getZ())*(point.getZ()-forwards[1].getZ())), forwards[1].getY()));
			System.out.println("   - " + heightResult);*/
			
			if(heightResult != heightResult) continue;
			
			totalSum += heightResult;
			successfulCalculationCount++;
			/* 
			 * this method is spline-average algorithm -- which averages all of the spline function results -- 
			 * might not be as accurate as the method written in this site:
			 * http://www.scielo.org.co/scielo.php?script=sci_arttext&pid=S1794-61902016000200008#f5
			 * TODO: change this method
			*/
		}
		return totalSum / (double) successfulCalculationCount;
	}
	
	
	/*public static void main(String[] args) {
		
		DXFMapContour[] contours = new DXFMapContour[] {
				new DXFMapContour(new Vector2D[] {
						new Vector2D(3, -3),
						new Vector2D(3, 3),
						new Vector2D(6, 3),
						new Vector2D(6, -3),
						new Vector2D(3, -3)
				}, 5),
				new DXFMapContour(new Vector2D[] {
						new Vector2D(2, -4),
						new Vector2D(2, 4),
						new Vector2D(7, 4),
						new Vector2D(7, -4),
						new Vector2D(2, -4)
				}, 3)
		};
		
		Vector2D rayStart = new Vector2D(2.5, 0), rayDelta = new Vector2D(2, 1);
		
		List<Entry<Vector3D, Double>> intersections;
		intersections = contours[0].getRayIntersections(rayStart, rayDelta);
		System.out.println(intersections);
		intersections = contours[1].getRayIntersections(rayStart, rayDelta);
		System.out.println(intersections);
		double height = getPointHeightFromContourList(rayStart, Arrays.asList(contours));
		System.out.println(height);
		
	}*/
}
