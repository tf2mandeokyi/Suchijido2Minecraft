package com.mndk.scjd2mc.core.triangulator;

import com.mndk.scjd2mc.core.scjd.SuchijidoData;
import com.mndk.scjd2mc.core.scjd.elem.ScjdContour;
import com.mndk.scjd2mc.core.scjd.elem.ScjdElement;
import com.mndk.scjd2mc.core.scjd.elem.ScjdElevationPoint;
import com.mndk.scjd2mc.core.scjd.elem.ScjdLayer;
import com.mndk.scjd2mc.core.scjd.geometry.GeometryShape;
import com.mndk.scjd2mc.core.scjd.geometry.LineString;
import com.mndk.scjd2mc.core.scjd.geometry.Polygon;
import com.mndk.scjd2mc.core.scjd.type.ElementDataType;
import com.mndk.scjd2mc.core.triangulator.cdt.ConstraintDelaunayTriangulator;
import com.mndk.scjd2mc.core.triangulator.cdt.IndexEdge;
import com.mndk.scjd2mc.core.util.math.Vector2DH;
import com.mndk.scjd2mc.core.util.shape.TriangleList;

import java.util.*;
import java.util.stream.Collectors;

public class TerrainTriangulator {



	public static TriangleList generateTerrain(SuchijidoData result) {

		Map.Entry<List<ScjdContour>, List<Vector2DH>> extraction = extractContourAndElevationPointList(result);

		TriangleList first = generateTerrain(extraction.getKey(), extraction.getValue(), Collections.emptyList());

		TriangleList second = generateWithLayerPolylines(result, extraction, first, ElementDataType.도로중심선);

		return generateWithLayerPolylines(result, extraction, second, ElementDataType.도로경계);

	}



	@SuppressWarnings("unchecked")
	private static TriangleList generateWithLayerPolylines(
			SuchijidoData result,
			Map.Entry<List<ScjdContour>, List<Vector2DH>> extraction,
			TriangleList previousResult,
			ElementDataType layerType) {

		List<ScjdElement<LineString>> roadCenterLines = new ArrayList<>();

		ScjdLayer tempLayer;
		if((tempLayer = result.getLayer(layerType)) != null) {
			roadCenterLines = tempLayer.stream().map(element -> (ScjdElement<LineString>) element).collect(Collectors.toList());
		}

		List<Vector2DH[]> roadCenterLineEdges = new ArrayList<>();

		for(ScjdElement<LineString> line : roadCenterLines) {
			Vector2DH[] vertex = line.getShape().getShape();
			for(int i = 0; i < vertex.length - 1; ++i) {
				Vector2DH p0 = vertex[i], p1 = vertex[i + 1];

				p0 = p0.withHeight(previousResult.interpolateHeight(p0.x, p0.z));
				p1 = p1.withHeight(previousResult.interpolateHeight(p1.x, p1.z));

				if(Double.isNaN(p0.height) || Double.isNaN(p1.height)) continue;

				roadCenterLineEdges.add(new Vector2DH[] { p0, p1 });
			}
		}

		return generateTerrain(extraction.getKey(), extraction.getValue(), roadCenterLineEdges);

	}
	
	
	
	private static TriangleList generateTerrain(
			List<ScjdContour> contours, List<Vector2DH> elevationPoints, List<Vector2DH[]> additionalEdges) {

		List<Vector2DH[]> vertexes = new ArrayList<>();

		for (ScjdContour contour : contours) {
			Vector2DH[] vertex = contour.getShape().getShape();
			Vector2DH[] destination = new Vector2DH[vertex.length];

			for (int j = 0; j < vertex.length; j++) {
				destination[j] = vertex[j].withHeight(contour.elevation);
			}

			vertexes.add(destination);
		}

		vertexes.addAll(additionalEdges);

		Map.Entry<List<Vector2DH>, List<IndexEdge>> parsed = vertexesToPointsAndIntegers(
				vertexes, elevationPoints.toArray(new Vector2DH[0]));

		ConstraintDelaunayTriangulator cdt = new ConstraintDelaunayTriangulator();

		cdt.insertVertices(parsed.getKey());
		cdt.insertEdges(parsed.getValue());

		cdt.eraseSuperTriangle();

		return cdt.getTriangles();
	}



	static Map.Entry<List<ScjdContour>, List<Vector2DH>> extractContourAndElevationPointList(SuchijidoData result) {

		List<ScjdContour> contourList = new ArrayList<>();
		List<Vector2DH> elevationPoints = new ArrayList<>();
		ScjdLayer tempLayer;

		if((tempLayer = result.getLayer(ElementDataType.등고선)) != null) {
			contourList = tempLayer.stream().map(element -> (ScjdContour) element).collect(Collectors.toList());
		}

		if((tempLayer = result.getLayer(ElementDataType.표고점)) != null) {
			for(ScjdElement<?> pElem : tempLayer) {
				assert pElem instanceof ScjdElevationPoint;
				Vector2DH point = ((ScjdElevationPoint) pElem).getShape().getShape();

				if( checkLayerContainsPoint(point, result.getLayer(ElementDataType.육교)) ||
						checkLayerContainsPoint(point, result.getLayer(ElementDataType.교량)) ||
						checkLayerContainsPoint(point, result.getLayer(ElementDataType.입체교차부))) {

					continue;
				}

				elevationPoints.add(point);
			}
		}

		return new AbstractMap.SimpleEntry<>(contourList, elevationPoints);

	}



	private static Map.Entry<List<Vector2DH>, List<IndexEdge>> vertexesToPointsAndIntegers(
			List<Vector2DH[]> vertexes, Vector2DH[] additionalPoints) {

		Map<Vector2DH, Integer> tempMap = new HashMap<>();
		int i = 0;

		List<IndexEdge> indexEdges = new ArrayList<>();
		for (Vector2DH[] vertex : vertexes) {

			Vector2DH point = vertex[0];
			Integer prevIndex = tempMap.get(point), index;

			if (prevIndex == null) {
				tempMap.put(point, i);
				prevIndex = i++;
			}

			for (int k = 1; k < vertex.length; k++) {
				point = vertex[k];
				index = tempMap.get(point);

				if (index == null) {
					tempMap.put(point, i);
					index = i++;
				}

				indexEdges.add(new IndexEdge(prevIndex, index));

				prevIndex = index;
			}
		}

		for (Vector2DH point : additionalPoints) {
			tempMap.put(point, i++);
		}

		Vector2DH[] pointArray = new Vector2DH[tempMap.size()];

		for(Map.Entry<Vector2DH, Integer> entry : tempMap.entrySet()) {
			pointArray[entry.getValue()] = entry.getKey();
		}

		return new AbstractMap.SimpleEntry<>(Arrays.asList(pointArray), indexEdges);

	}


	private static boolean checkLayerContainsPoint(Vector2DH point, ScjdLayer layer) {
		if(layer == null) return true;

		for(ScjdElement<?> elem : layer) {
			GeometryShape<?> shape = elem.getShape();
			if(shape instanceof Polygon) {
				if(((Polygon) shape).containsPoint(point)) {
					return true;
				}
			}
		}
		return false;
	}
}
