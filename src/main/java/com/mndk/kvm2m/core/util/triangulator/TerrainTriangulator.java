package com.mndk.kvm2m.core.util.triangulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.shape.Triangle;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vmap.VMapElementType;
import com.mndk.kvm2m.core.vmap.VMapParserResult;
import com.mndk.kvm2m.core.vmap.elem.line.VMapContour;
import com.mndk.kvm2m.core.vmap.elem.line.VMapContourList;
import com.mndk.kvm2m.core.vmap.elem.point.VMapElevationPoint;

public class TerrainTriangulator {
	
	
	public static TriangleList generate(VMapParserResult result) {
		
		List<VMapContour> contourList = 
				result.getLayer(VMapElementType.등고선).stream().map(element -> (VMapContour) element).collect(Collectors.toList());
		List<Vector2DH> elevationPoints = 
				result.getLayer(VMapElementType.표고점).stream().map(element -> ((VMapElevationPoint) element).toVector()).collect(Collectors.toList());
		
		return generate(contourList, elevationPoints);
	}
	
	
	
	public static TriangleList generate(List<VMapContour> contours, List<Vector2DH> elevationPoints) {
		Vector2DH[][] vertexes = new Vector2DH[contours.size()][];
		
		for(int i = 0; i < contours.size(); i++) {
			VMapContour contour = contours.get(i);
			Vector2DH[] vertex = contour.getVertexList()[0];
			Vector2DH[] destination = vertexes[i] = new Vector2DH[vertex.length];
			
			for(int j = 0; j < vertex.length; j++) {
				destination[j] = vertex[j].withHeight(contour.elevation);
			}
		}
		
		return new ConstraintDelaunayTriangulator(vertexes, elevationPoints.toArray(new Vector2DH[0])).getTriangleList();
	}
	
	
	
	public static TriangleList generate_(List<VMapContour> contours, List<Vector2DH> elevationPoints) {
		
		TriangleList result = new TriangleList();
		Map<Integer, List<VMapContour>> categorized = categorizeContours(contours);
		
		List<Integer> keyList = new ArrayList<>(categorized.keySet());
		Collections.sort(keyList);
		
		List<Vector2DH> tempPointList;
		for(int i = 0; i < keyList.size(); i++) {
			
			tempPointList = new ArrayList<>();

			int current = keyList.get(i), lower = 0, higher = 0;
			if(i != 0) lower = keyList.get(i-1);
			if(i != keyList.size() - 1) higher = keyList.get(i+1);
			
			int[] contourHeightArray;
			if(i == 0 && i == keyList.size() - 1) contourHeightArray = new int[] { current };
			else if(i == 0) contourHeightArray = new int[] { current, higher };
			else if(i == keyList.size() - 1) contourHeightArray = new int[] { lower, current };
			else contourHeightArray = new int[] { lower, current, higher };
			
			// int contourCount = contourHeightArray.length;
			
			for(int h : contourHeightArray) {
				for(VMapContour contour : categorized.get(h)) {
					Vector2DH[] contourVertexArray = contour.getVertexList()[0];
					for(int k = 0; k < contourVertexArray.length; k++) {
						tempPointList.add(contourVertexArray[k].withHeight(contour.elevation));
					}
				}
			}
			
			List<Triangle> delaunayResult = new FastDelaunayTriangulator(tempPointList).getTriangleList();
			
			int h1, h2, h3;
			triangleLoop: for(Triangle triangle : delaunayResult) {
				
				h1 = (int) Math.round(triangle.v1.height);
				h2 = (int) Math.round(triangle.v2.height);
				h3 = (int) Math.round(triangle.v3.height);
				
				// System.out.println("Checking triangle: v1=" + triangle.v1 + ", v2=" + triangle.v2 + ", v3=" + triangle.v3 + ", current=" + current);
				
				// Check if at least one triangle point's height value is the same with the current height calculation
				if(h1 == current || h2 == current || h3 == current) {
					
					// Check if the triangle intersects the contour line
					for(int h : contourHeightArray) {
						for(VMapContour contour : categorized.get(h)) {
							Vector2DH[] contourVertexArray = contour.getVertexList()[0];
							for(int k = 0; k < contourVertexArray.length; k++) {
								if(k != contourVertexArray.length - 1) {
									if(triangle.checkIntersection(contourVertexArray[k], contourVertexArray[k+1])) {
										continue triangleLoop;
									}
								}
								else {
									if(triangle.checkIntersection(contourVertexArray[k], contourVertexArray[0])) {
										continue triangleLoop;
									}
								}
							}
						}
					}
					
					result.add(triangle);
					
					/*if(contourCount == 3) {
						int currentElevationPointCount = (h1==current?1:0) + (h2==current?1:0) + (h3==current?1:0);
						if(currentElevationPointCount > 1) {
							result.add(triangle);
						}
						else if(currentElevationPointCount == 1) {
							if(h1 == current) if(h2 != h3) continue;
							if(h2 == current) if(h1 != h3) continue;
							if(h3 == current) if(h1 != h2) continue;
							result.add(triangle);
						}
						else {
							continue;
						}
					}
					else {
						result.add(triangle);
					}*/
				}
			}
			
		}
		
		return result;
		
	}
	
	
	private static Map<Integer, List<VMapContour>> categorizeContours(List<VMapContour> contours) {
		
		Map<Integer, List<VMapContour>> result = new HashMap<>();
		
		for(VMapContour contour : contours) {
			int elevation = contour.elevation;
			if(result.containsKey(elevation)) {
				result.get(elevation).add(contour);
			}
			else {
				result.put(elevation, new VMapContourList(contour));
			}
		}
		
		return result;
		
	}
	
}
